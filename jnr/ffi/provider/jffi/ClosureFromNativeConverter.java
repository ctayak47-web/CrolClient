
package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Invoker;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import jnr.ffi.CallingConvention;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.mapper.DefaultSignatureType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.MethodResultContext;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.provider.InAccessibleMemoryIO;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.SigType;
import jnr.ffi.provider.jffi.AsmBuilder;
import jnr.ffi.provider.jffi.AsmClassLoader;
import jnr.ffi.provider.jffi.AsmLibraryLoader;
import jnr.ffi.provider.jffi.AsmUtil;
import jnr.ffi.provider.jffi.BaseMethodGenerator;
import jnr.ffi.provider.jffi.BufferMethodGenerator;
import jnr.ffi.provider.jffi.ClosureUtil;
import jnr.ffi.provider.jffi.CodegenUtils;
import jnr.ffi.provider.jffi.FastIntMethodGenerator;
import jnr.ffi.provider.jffi.FastLongMethodGenerator;
import jnr.ffi.provider.jffi.FastNumericMethodGenerator;
import jnr.ffi.provider.jffi.InvokerUtil;
import jnr.ffi.provider.jffi.LocalVariableAllocator;
import jnr.ffi.provider.jffi.SkinnyMethodAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public abstract class ClosureFromNativeConverter
implements FromNativeConverter<Object, Pointer> {
    private static final AtomicLong nextClassID = new AtomicLong(0L);

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    public static FromNativeConverter<?, Pointer> getInstance(Runtime runtime, SignatureType type, AsmClassLoader classLoader, SignatureTypeMapper typeMapper) {
        return ClosureFromNativeConverter.newClosureConverter(runtime, classLoader, type.getDeclaredType(), typeMapper);
    }

    private static FromNativeConverter newClosureConverter(Runtime runtime, AsmClassLoader classLoader, Class closureClass, SignatureTypeMapper typeMapper) {
        ClassWriter cw = new ClassWriter(2);
        ClassWriter cv = AsmLibraryLoader.DEBUG ? AsmUtil.newCheckClassAdapter((ClassVisitor)cw) : cw;
        String className = CodegenUtils.p(closureClass) + "$jnr$fromNativeConverter$" + nextClassID.getAndIncrement();
        AsmBuilder builder = new AsmBuilder(runtime, className, (ClassVisitor)cv, classLoader);
        cv.visit(52, 17, className, null, CodegenUtils.p(AbstractClosurePointer.class), new String[]{CodegenUtils.p(closureClass)});
        cv.visitAnnotation(CodegenUtils.ci(FromNativeConverter.NoContext.class), true);
        ClosureFromNativeConverter.generateInvocation(runtime, builder, closureClass, typeMapper);
        SkinnyMethodAdapter init = new SkinnyMethodAdapter((ClassVisitor)cv, 1, "<init>", CodegenUtils.sig(Void.TYPE, Runtime.class, Long.TYPE, Object[].class), null, null);
        init.start();
        init.aload(0);
        init.aload(1);
        init.lload(2);
        init.invokespecial(CodegenUtils.p(AbstractClosurePointer.class), "<init>", CodegenUtils.sig(Void.TYPE, Runtime.class, Long.TYPE));
        builder.emitFieldInitialization(init, 4);
        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();
        Class implClass = ClosureFromNativeConverter.loadClass(classLoader, className, cw);
        try {
            return new ProxyConverter(runtime, implClass.getConstructor(Runtime.class, Long.TYPE, Object[].class), builder.getObjectFieldValues());
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Class loadClass(AsmClassLoader classLoader, String className, ClassWriter cw) {
        try {
            byte[] bytes = cw.toByteArray();
            if (AsmLibraryLoader.DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }
            return classLoader.defineClass(className.replace("/", "."), bytes);
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void generateInvocation(Runtime runtime, AsmBuilder builder, Class closureClass, SignatureTypeMapper typeMapper) {
        BaseMethodGenerator[] generators;
        Method closureMethod = ClosureUtil.getDelegateMethod(closureClass);
        MethodResultContext resultContext = new MethodResultContext(runtime, closureMethod);
        DefaultSignatureType signatureType = DefaultSignatureType.create(closureMethod.getReturnType(), resultContext);
        FromNativeType fromNativeType = typeMapper.getFromNativeType(signatureType, resultContext);
        FromNativeConverter fromNativeConverter = fromNativeType != null ? fromNativeType.getFromNativeConverter() : null;
        ResultType resultType = InvokerUtil.getResultType(runtime, closureMethod.getReturnType(), resultContext.getAnnotations(), fromNativeConverter, (FromNativeContext)resultContext);
        SigType[] parameterTypes = InvokerUtil.getParameterTypes(runtime, typeMapper, closureMethod);
        CallingConvention callingConvention = closureClass.isAnnotationPresent(StdCall.class) ? CallingConvention.STDCALL : CallingConvention.DEFAULT;
        CallContext callContext = InvokerUtil.getCallContext(resultType, parameterTypes, callingConvention, true);
        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(parameterTypes);
        Class[] javaParameterTypes = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            javaParameterTypes[i] = parameterTypes[i].getDeclaredType();
        }
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), 17, closureMethod.getName(), CodegenUtils.sig(resultType.getDeclaredType(), javaParameterTypes), null, null);
        mv.start();
        mv.getstatic(CodegenUtils.p(AbstractClosurePointer.class), "ffi", CodegenUtils.ci(Invoker.class));
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(callContext), CodegenUtils.ci(CallContext.class));
        mv.aload(0);
        mv.getfield(CodegenUtils.p(AbstractClosurePointer.class), "functionAddress", CodegenUtils.ci(Long.TYPE));
        for (BaseMethodGenerator generator : generators = new BaseMethodGenerator[]{new FastIntMethodGenerator(), new FastLongMethodGenerator(), new FastNumericMethodGenerator(), new BufferMethodGenerator()}) {
            if (!generator.isSupported(resultType, (ParameterType[])parameterTypes, callingConvention)) continue;
            generator.generate(builder, mv, localVariableAllocator, callContext, resultType, (ParameterType[])parameterTypes, false);
        }
        mv.visitMaxs(100, 10 + localVariableAllocator.getSpaceUsed());
        mv.visitEnd();
    }

    public static abstract class AbstractClosurePointer
    extends InAccessibleMemoryIO {
        public static final Invoker ffi = Invoker.getInstance();
        protected final long functionAddress;

        protected AbstractClosurePointer(Runtime runtime, long functionAddress) {
            super(runtime, functionAddress, true);
            this.functionAddress = functionAddress;
        }

        @Override
        public final long size() {
            return 0L;
        }
    }

    public static final class ProxyConverter
    extends ClosureFromNativeConverter {
        private final Runtime runtime;
        private final Constructor closureConstructor;
        private final Object[] initFields;

        public ProxyConverter(Runtime runtime, Constructor closureConstructor, Object[] initFields) {
            this.runtime = runtime;
            this.closureConstructor = closureConstructor;
            this.initFields = (Object[])initFields.clone();
        }

        @Override
        public Object fromNative(Pointer nativeValue, FromNativeContext context) {
            try {
                return this.closureConstructor.newInstance(this.runtime, nativeValue.address(), this.initFields);
            }
            catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}

