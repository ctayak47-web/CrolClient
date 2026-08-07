
package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import jnr.ffi.CallingConvention;
import jnr.ffi.LibraryOption;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.Synchronized;
import jnr.ffi.annotations.Variadic;
import jnr.ffi.mapper.CompositeTypeMapper;
import jnr.ffi.mapper.DefaultSignatureType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.mapper.MethodResultContext;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.provider.IdentityFunctionMapper;
import jnr.ffi.provider.InterfaceScanner;
import jnr.ffi.provider.Invoker;
import jnr.ffi.provider.NativeFunction;
import jnr.ffi.provider.NativeVariable;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.SigType;
import jnr.ffi.provider.jffi.AbstractAsmLibraryInterface;
import jnr.ffi.provider.jffi.AsmBuilder;
import jnr.ffi.provider.jffi.AsmClassLoader;
import jnr.ffi.provider.jffi.AsmRuntime;
import jnr.ffi.provider.jffi.AsmUtil;
import jnr.ffi.provider.jffi.BufferMethodGenerator;
import jnr.ffi.provider.jffi.CodegenUtils;
import jnr.ffi.provider.jffi.DefaultInvokerFactory;
import jnr.ffi.provider.jffi.FastIntMethodGenerator;
import jnr.ffi.provider.jffi.FastLongMethodGenerator;
import jnr.ffi.provider.jffi.FastNumericMethodGenerator;
import jnr.ffi.provider.jffi.InvokerUtil;
import jnr.ffi.provider.jffi.LibraryLoader;
import jnr.ffi.provider.jffi.MethodGenerator;
import jnr.ffi.provider.jffi.NativeFunctionMapperContext;
import jnr.ffi.provider.jffi.NativeLibrary;
import jnr.ffi.provider.jffi.NativeRuntime;
import jnr.ffi.provider.jffi.NoTrace;
import jnr.ffi.provider.jffi.NoX86;
import jnr.ffi.provider.jffi.NotImplMethodGenerator;
import jnr.ffi.provider.jffi.SkinnyMethodAdapter;
import jnr.ffi.provider.jffi.StubCompiler;
import jnr.ffi.provider.jffi.SymbolNotFoundError;
import jnr.ffi.provider.jffi.VariableAccessorGenerator;
import jnr.ffi.provider.jffi.X86MethodGenerator;
import jnr.ffi.util.Annotations;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class AsmLibraryLoader
extends LibraryLoader {
    public static final boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0L);
    private static final AtomicLong uniqueId = new AtomicLong(0L);
    private static final ThreadLocal<AsmClassLoader> classLoader = new ThreadLocal();
    private final NativeRuntime runtime = NativeRuntime.getInstance();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, boolean failImmediately) {
        AsmClassLoader oldClassLoader = classLoader.get();
        if (oldClassLoader == null) {
            classLoader.set(new AsmClassLoader(interfaceClass.getClassLoader()));
        }
        try {
            T t = this.generateInterfaceImpl(library, interfaceClass, libraryOptions, classLoader.get());
            return t;
        }
        finally {
            if (oldClassLoader == null) {
                classLoader.remove();
            }
        }
    }

    private <T> T generateInterfaceImpl(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, AsmClassLoader classLoader) {
        boolean debug = DEBUG && !interfaceClass.isAnnotationPresent(NoTrace.class);
        ClassWriter cw = new ClassWriter(2);
        ClassWriter cv = debug ? AsmUtil.newCheckClassAdapter((ClassVisitor)cw) : cw;
        AsmBuilder builder = new AsmBuilder(this.runtime, CodegenUtils.p(interfaceClass) + "$jnr$ffi$" + nextClassID.getAndIncrement(), (ClassVisitor)cv, classLoader);
        cv.visit(52, 17, builder.getClassNamePath(), null, CodegenUtils.p(AbstractAsmLibraryInterface.class), new String[]{CodegenUtils.p(interfaceClass)});
        FunctionMapper functionMapper = libraryOptions.containsKey((Object)LibraryOption.FunctionMapper) ? (FunctionMapper)libraryOptions.get((Object)LibraryOption.FunctionMapper) : IdentityFunctionMapper.getInstance();
        SignatureTypeMapper typeMapper = AsmLibraryLoader.getSignatureTypeMapper(libraryOptions);
        CompositeTypeMapper closureTypeMapper = AsmLibraryLoader.newClosureTypeMapper(classLoader, typeMapper);
        typeMapper = AsmLibraryLoader.newCompositeTypeMapper(this.runtime, classLoader, typeMapper, closureTypeMapper);
        CallingConvention libraryCallingConvention = InvokerUtil.getCallingConvention(interfaceClass, libraryOptions);
        StubCompiler compiler = StubCompiler.newCompiler(this.runtime);
        MethodGenerator[] generators = new MethodGenerator[]{!interfaceClass.isAnnotationPresent(NoX86.class) ? new X86MethodGenerator(compiler) : new NotImplMethodGenerator(), new FastIntMethodGenerator(), new FastLongMethodGenerator(), new FastNumericMethodGenerator(), new BufferMethodGenerator()};
        DefaultInvokerFactory invokerFactory = new DefaultInvokerFactory(this.runtime, library, typeMapper, functionMapper, libraryCallingConvention, libraryOptions, interfaceClass.isAnnotationPresent(Synchronized.class));
        InterfaceScanner scanner = new InterfaceScanner(interfaceClass, typeMapper, libraryCallingConvention);
        block6: for (NativeFunction nativeFunction : scanner.functions()) {
            Method method = nativeFunction.getMethod();
            if (method.isVarArgs() || method.isAnnotationPresent(Variadic.class)) {
                AsmBuilder.ObjectField field = builder.getObjectField(invokerFactory.createInvoker(method), Invoker.class);
                this.generateVarargsInvocation(builder, method, field);
                continue;
            }
            String functionName = functionMapper.mapFunctionName(nativeFunction.name(), new NativeFunctionMapperContext(library, nativeFunction.annotations()));
            try {
                long functionAddress = library.findSymbolAddress(functionName);
                MethodResultContext resultContext = new MethodResultContext(this.runtime, method);
                DefaultSignatureType signatureType = DefaultSignatureType.create(method.getReturnType(), resultContext);
                ResultType resultType = InvokerUtil.getResultType((Runtime)this.runtime, method.getReturnType(), resultContext.getAnnotations(), typeMapper.getFromNativeType(signatureType, resultContext), (FromNativeContext)resultContext);
                SigType[] parameterTypes = InvokerUtil.getParameterTypes(this.runtime, typeMapper, method);
                boolean saveError = jnr.ffi.LibraryLoader.saveError(libraryOptions, nativeFunction.hasSaveError(), nativeFunction.hasIgnoreError());
                Function jffiFunction = new Function(functionAddress, InvokerUtil.getCallContext(resultType, parameterTypes, nativeFunction.convention(), saveError));
                for (MethodGenerator g : generators) {
                    if (!g.isSupported(resultType, (ParameterType[])parameterTypes, nativeFunction.convention())) continue;
                    g.generate(builder, method.getName(), jffiFunction, resultType, (ParameterType[])parameterTypes, !saveError);
                    continue block6;
                }
            }
            catch (SymbolNotFoundError ex) {
                String errorFieldName = "error_" + uniqueId.incrementAndGet();
                cv.visitField(26, errorFieldName, CodegenUtils.ci(String.class), null, (Object)ex.getMessage());
                this.generateFunctionNotFound((ClassVisitor)cv, builder.getClassNamePath(), errorFieldName, functionName, method.getReturnType(), method.getParameterTypes());
            }
        }
        VariableAccessorGenerator variableAccessorGenerator = new VariableAccessorGenerator(this.runtime);
        for (NativeVariable v : scanner.variables()) {
            Method m = v.getMethod();
            Type variableType = ((ParameterizedType)m.getGenericReturnType()).getActualTypeArguments()[0];
            if (!(variableType instanceof Class)) {
                throw new IllegalArgumentException("unsupported variable class: " + variableType);
            }
            String functionName = functionMapper.mapFunctionName(m.getName(), null);
            try {
                variableAccessorGenerator.generate(builder, interfaceClass, m.getName(), library.findSymbolAddress(functionName), (Class)variableType, Annotations.sortedAnnotationCollection(m.getAnnotations()), typeMapper, classLoader);
            }
            catch (SymbolNotFoundError ex) {
                String errorFieldName = "error_" + uniqueId.incrementAndGet();
                cv.visitField(26, errorFieldName, CodegenUtils.ci(String.class), null, (Object)ex.getMessage());
                this.generateFunctionNotFound((ClassVisitor)cv, builder.getClassNamePath(), errorFieldName, functionName, m.getReturnType(), m.getParameterTypes());
            }
        }
        SkinnyMethodAdapter skinnyMethodAdapter = new SkinnyMethodAdapter((ClassVisitor)cv, 1, "<init>", CodegenUtils.sig(Void.TYPE, Runtime.class, NativeLibrary.class, Object[].class), null, null);
        skinnyMethodAdapter.start();
        skinnyMethodAdapter.aload(0);
        skinnyMethodAdapter.aload(1);
        skinnyMethodAdapter.aload(2);
        skinnyMethodAdapter.invokespecial(CodegenUtils.p(AbstractAsmLibraryInterface.class), "<init>", CodegenUtils.sig(Void.TYPE, Runtime.class, NativeLibrary.class));
        builder.emitFieldInitialization(skinnyMethodAdapter, 3);
        skinnyMethodAdapter.voidreturn();
        skinnyMethodAdapter.visitMaxs(10, 10);
        skinnyMethodAdapter.visitEnd();
        cv.visitEnd();
        try {
            byte[] bytes = cw.toByteArray();
            if (debug) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }
            Class implClass = classLoader.defineClass(builder.getClassNamePath().replace("/", "."), bytes);
            Constructor cons = implClass.getDeclaredConstructor(Runtime.class, NativeLibrary.class, Object[].class);
            Object result = cons.newInstance(this.runtime, library, builder.getObjectFieldValues());
            System.err.flush();
            System.out.flush();
            compiler.attach(implClass);
            return result;
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private void generateFunctionNotFound(ClassVisitor cv, String className, String errorFieldName, String functionName, Class returnType, Class[] parameterTypes) {
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv, 17, functionName, CodegenUtils.sig(returnType, parameterTypes), null, null);
        mv.start();
        mv.getstatic(className, errorFieldName, CodegenUtils.ci(String.class));
        mv.invokestatic(AsmRuntime.class, "newUnsatisifiedLinkError", UnsatisfiedLinkError.class, String.class);
        mv.athrow();
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    private void generateVarargsInvocation(AsmBuilder builder, Method m, AsmBuilder.ObjectField field) {
        Class[] parameterTypes = m.getParameterTypes();
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), 17, m.getName(), CodegenUtils.sig(m.getReturnType(), parameterTypes), null, null);
        mv.start();
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), field.name, CodegenUtils.ci(Invoker.class));
        mv.aload(0);
        mv.pushInt(parameterTypes.length);
        mv.anewarray(CodegenUtils.p(Object.class));
        int slot = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            mv.dup();
            mv.pushInt(i);
            if (parameterTypes[i].equals(Long.TYPE)) {
                mv.lload(slot);
                mv.invokestatic(Long.class, "valueOf", Long.class, Long.TYPE);
                ++slot;
            } else if (parameterTypes[i].equals(Double.TYPE)) {
                mv.dload(slot);
                mv.invokestatic(Double.class, "valueOf", Double.class, Double.TYPE);
                ++slot;
            } else if (parameterTypes[i].equals(Integer.TYPE)) {
                mv.iload(slot);
                mv.invokestatic(Integer.class, "valueOf", Integer.class, Integer.TYPE);
            } else if (parameterTypes[i].equals(Float.TYPE)) {
                mv.fload(slot);
                mv.invokestatic(Float.class, "valueOf", Float.class, Float.TYPE);
            } else if (parameterTypes[i].equals(Short.TYPE)) {
                mv.iload(slot);
                mv.i2s();
                mv.invokestatic(Short.class, "valueOf", Short.class, Short.TYPE);
            } else if (parameterTypes[i].equals(Character.TYPE)) {
                mv.iload(slot);
                mv.i2c();
                mv.invokestatic(Character.class, "valueOf", Character.class, Character.TYPE);
            } else if (parameterTypes[i].equals(Byte.TYPE)) {
                mv.iload(slot);
                mv.i2b();
                mv.invokestatic(Byte.class, "valueOf", Byte.class, Byte.TYPE);
            } else if (parameterTypes[i].equals(Character.TYPE)) {
                mv.iload(slot);
                mv.i2b();
                mv.invokestatic(Boolean.class, "valueOf", Boolean.class, Boolean.TYPE);
            } else {
                mv.aload(slot);
            }
            mv.aastore();
            ++slot;
        }
        mv.invokeinterface(Invoker.class, "invoke", Object.class, Object.class, Object[].class);
        Class<?> returnType = m.getReturnType();
        if (returnType.equals(Long.TYPE)) {
            mv.checkcast(Long.class);
            mv.invokevirtual(Long.class, "longValue", Long.TYPE, new Class[0]);
            mv.lreturn();
        } else if (returnType.equals(Double.TYPE)) {
            mv.checkcast(Double.class);
            mv.invokevirtual(Double.class, "doubleValue", Double.TYPE, new Class[0]);
            mv.dreturn();
        } else if (returnType.equals(Integer.TYPE)) {
            mv.checkcast(Integer.class);
            mv.invokevirtual(Integer.class, "intValue", Integer.TYPE, new Class[0]);
            mv.ireturn();
        } else if (returnType.equals(Float.TYPE)) {
            mv.checkcast(Float.class);
            mv.invokevirtual(Float.class, "floatValue", Float.TYPE, new Class[0]);
            mv.freturn();
        } else if (returnType.equals(Short.TYPE)) {
            mv.checkcast(Short.class);
            mv.invokevirtual(Short.class, "shortValue", Short.TYPE, new Class[0]);
            mv.ireturn();
        } else if (returnType.equals(Character.TYPE)) {
            mv.checkcast(Character.class);
            mv.invokevirtual(Character.class, "charValue", Character.TYPE, new Class[0]);
            mv.ireturn();
        } else if (returnType.equals(Byte.TYPE)) {
            mv.checkcast(Byte.class);
            mv.invokevirtual(Byte.class, "byteValue", Byte.TYPE, new Class[0]);
            mv.ireturn();
        } else if (returnType.equals(Boolean.TYPE)) {
            mv.checkcast(Boolean.class);
            mv.invokevirtual(Boolean.class, "booleanValue", Boolean.TYPE, new Class[0]);
            mv.ireturn();
        } else if (Void.TYPE.isAssignableFrom(m.getReturnType())) {
            mv.voidreturn();
        } else {
            mv.checkcast(m.getReturnType());
            mv.areturn();
        }
        mv.visitMaxs(100, AsmUtil.calculateLocalVariableSpace(parameterTypes) + 1);
        mv.visitEnd();
    }
}

