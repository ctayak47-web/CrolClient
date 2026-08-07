
package jnr.ffi.provider.jffi;

import com.kenai.jffi.Platform;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import jnr.ffi.Address;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.FromNativeType;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.SigType;
import jnr.ffi.provider.ToNativeType;
import jnr.ffi.provider.jffi.AsmBuilder;
import jnr.ffi.provider.jffi.AsmClassLoader;
import jnr.ffi.provider.jffi.AsmRuntime;
import jnr.ffi.provider.jffi.CodegenUtils;
import jnr.ffi.provider.jffi.LocalVariable;
import jnr.ffi.provider.jffi.NumberUtil;
import jnr.ffi.provider.jffi.SkinnyMethodAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

final class AsmUtil {
    private AsmUtil() {
    }

    public static MethodVisitor newTraceMethodVisitor(MethodVisitor mv) {
        try {
            Class<MethodVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceMethodVisitor").asSubclass(MethodVisitor.class);
            Constructor<MethodVisitor> c = tmvClass.getDeclaredConstructor(MethodVisitor.class);
            return c.newInstance(mv);
        }
        catch (Throwable t) {
            return mv;
        }
    }

    public static ClassVisitor newTraceClassVisitor(ClassVisitor cv, OutputStream out) {
        return AsmUtil.newTraceClassVisitor(cv, new PrintWriter(out, true));
    }

    public static ClassVisitor newTraceClassVisitor(ClassVisitor cv, PrintWriter out) {
        try {
            Class<ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class, PrintWriter.class);
            return c.newInstance(cv, out);
        }
        catch (Throwable t) {
            return cv;
        }
    }

    public static ClassVisitor newTraceClassVisitor(PrintWriter out) {
        try {
            Class<ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<ClassVisitor> c = tmvClass.getDeclaredConstructor(PrintWriter.class);
            return c.newInstance(out);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static ClassVisitor newCheckClassAdapter(ClassVisitor cv) {
        try {
            Class<ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.CheckClassAdapter").asSubclass(ClassVisitor.class);
            Constructor<ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class);
            return c.newInstance(cv);
        }
        catch (Throwable t) {
            return cv;
        }
    }

    public static Class unboxedReturnType(Class type) {
        return AsmUtil.unboxedType(type);
    }

    public static Class unboxedType(Class boxedType) {
        if (boxedType == Byte.class) {
            return Byte.TYPE;
        }
        if (boxedType == Short.class) {
            return Short.TYPE;
        }
        if (boxedType == Integer.class) {
            return Integer.TYPE;
        }
        if (boxedType == Long.class) {
            return Long.TYPE;
        }
        if (boxedType == Float.class) {
            return Float.TYPE;
        }
        if (boxedType == Double.class) {
            return Double.TYPE;
        }
        if (boxedType == Boolean.class) {
            return Boolean.TYPE;
        }
        if (Pointer.class.isAssignableFrom(boxedType)) {
            return Platform.getPlatform().addressSize() == 32 ? Integer.TYPE : Long.TYPE;
        }
        if (Address.class == boxedType) {
            return Platform.getPlatform().addressSize() == 32 ? Integer.TYPE : Long.TYPE;
        }
        return boxedType;
    }

    public static Class boxedType(Class type) {
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        return type;
    }

    static void emitReturnOp(SkinnyMethodAdapter mv, Class returnType) {
        if (!returnType.isPrimitive()) {
            mv.areturn();
        } else if (Long.TYPE == returnType) {
            mv.lreturn();
        } else if (Float.TYPE == returnType) {
            mv.freturn();
        } else if (Double.TYPE == returnType) {
            mv.dreturn();
        } else if (Void.TYPE == returnType) {
            mv.voidreturn();
        } else {
            mv.ireturn();
        }
    }

    static int calculateLocalVariableSpace(Class type) {
        return Long.TYPE == type || Double.TYPE == type ? 2 : 1;
    }

    static int calculateLocalVariableSpace(SigType type) {
        return AsmUtil.calculateLocalVariableSpace(type.getDeclaredType());
    }

    static int calculateLocalVariableSpace(Class ... types) {
        int size = 0;
        for (int i = 0; i < types.length; ++i) {
            size += AsmUtil.calculateLocalVariableSpace(types[i]);
        }
        return size;
    }

    static int calculateLocalVariableSpace(SigType ... types) {
        int size = 0;
        for (SigType type : types) {
            size += AsmUtil.calculateLocalVariableSpace(type);
        }
        return size;
    }

    private static void unboxPointerOrStruct(SkinnyMethodAdapter mv, Class type, Class nativeType) {
        mv.invokestatic(CodegenUtils.p(AsmRuntime.class), Long.TYPE == nativeType ? "longValue" : "intValue", CodegenUtils.sig(nativeType, type));
    }

    static void unboxPointer(SkinnyMethodAdapter mv, Class nativeType) {
        AsmUtil.unboxPointerOrStruct(mv, Pointer.class, nativeType);
    }

    static void unboxBoolean(SkinnyMethodAdapter mv, Class boxedType, Class nativeType) {
        mv.invokevirtual(CodegenUtils.p(boxedType), "booleanValue", "()Z");
        NumberUtil.widen(mv, Boolean.TYPE, nativeType);
    }

    static void unboxBoolean(SkinnyMethodAdapter mv, Class nativeType) {
        AsmUtil.unboxBoolean(mv, Boolean.class, nativeType);
    }

    static void unboxNumber(SkinnyMethodAdapter mv, Class boxedType, Class unboxedType, NativeType nativeType) {
        if (Number.class.isAssignableFrom(boxedType)) {
            switch (nativeType) {
                case SCHAR: 
                case UCHAR: {
                    mv.invokevirtual(CodegenUtils.p(boxedType), "byteValue", "()B");
                    NumberUtil.convertPrimitive(mv, Byte.TYPE, unboxedType, nativeType);
                    break;
                }
                case SSHORT: 
                case USHORT: {
                    mv.invokevirtual(CodegenUtils.p(boxedType), "shortValue", "()S");
                    NumberUtil.convertPrimitive(mv, Short.TYPE, unboxedType, nativeType);
                    break;
                }
                case SINT: 
                case UINT: 
                case SLONG: 
                case ULONG: 
                case ADDRESS: {
                    if (NumberUtil.sizeof(nativeType) == 4) {
                        mv.invokevirtual(CodegenUtils.p(boxedType), "intValue", "()I");
                        NumberUtil.convertPrimitive(mv, Integer.TYPE, unboxedType, nativeType);
                        break;
                    }
                    mv.invokevirtual(CodegenUtils.p(boxedType), "longValue", "()J");
                    NumberUtil.convertPrimitive(mv, Long.TYPE, unboxedType, nativeType);
                    break;
                }
                case SLONGLONG: 
                case ULONGLONG: {
                    mv.invokevirtual(CodegenUtils.p(boxedType), "longValue", "()J");
                    NumberUtil.narrow(mv, Long.TYPE, unboxedType);
                    break;
                }
                case FLOAT: {
                    mv.invokevirtual(CodegenUtils.p(boxedType), "floatValue", "()F");
                    break;
                }
                case DOUBLE: {
                    mv.invokevirtual(CodegenUtils.p(boxedType), "doubleValue", "()D");
                }
            }
        } else if (Boolean.class.isAssignableFrom(boxedType)) {
            AsmUtil.unboxBoolean(mv, unboxedType);
        } else {
            throw new IllegalArgumentException("unsupported boxed type: " + boxedType);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static void unboxNumber(SkinnyMethodAdapter mv, Class boxedType, Class nativeType) {
        if (Number.class.isAssignableFrom(boxedType)) {
            if (Byte.TYPE == nativeType) {
                mv.invokevirtual(CodegenUtils.p(boxedType), "byteValue", "()B");
                return;
            } else if (Short.TYPE == nativeType) {
                mv.invokevirtual(CodegenUtils.p(boxedType), "shortValue", "()S");
                return;
            } else if (Integer.TYPE == nativeType) {
                mv.invokevirtual(CodegenUtils.p(boxedType), "intValue", "()I");
                return;
            } else if (Long.TYPE == nativeType) {
                mv.invokevirtual(CodegenUtils.p(boxedType), "longValue", "()J");
                return;
            } else if (Float.TYPE == nativeType) {
                mv.invokevirtual(CodegenUtils.p(boxedType), "floatValue", "()F");
                return;
            } else {
                if (Double.TYPE != nativeType) throw new IllegalArgumentException("unsupported Number subclass: " + boxedType);
                mv.invokevirtual(CodegenUtils.p(boxedType), "doubleValue", "()D");
            }
            return;
        } else {
            if (!Boolean.class.isAssignableFrom(boxedType)) throw new IllegalArgumentException("unsupported boxed type: " + boxedType);
            AsmUtil.unboxBoolean(mv, nativeType);
        }
    }

    static void boxValue(AsmBuilder builder, SkinnyMethodAdapter mv, Class boxedType, Class unboxedType) {
        if (boxedType != unboxedType && !boxedType.isPrimitive()) {
            if (Boolean.class.isAssignableFrom(boxedType)) {
                NumberUtil.narrow(mv, unboxedType, Boolean.TYPE);
                mv.invokestatic(Boolean.class, "valueOf", Boolean.class, Boolean.TYPE);
            } else if (Pointer.class.isAssignableFrom(boxedType)) {
                AsmUtil.getfield(mv, builder, builder.getRuntimeField());
                mv.invokestatic(AsmRuntime.class, "pointerValue", Pointer.class, unboxedType, Runtime.class);
            } else if (Address.class == boxedType) {
                mv.invokestatic(boxedType, "valueOf", boxedType, unboxedType);
            } else if (Number.class.isAssignableFrom(boxedType) && AsmUtil.boxedType(unboxedType) == boxedType) {
                mv.invokestatic(boxedType, "valueOf", boxedType, unboxedType);
            } else {
                throw new IllegalArgumentException("cannot box value of type " + unboxedType + " to " + boxedType);
            }
        }
    }

    static int getNativeArrayFlags(int flags) {
        int nflags = 0;
        nflags |= ParameterFlags.isIn(flags) ? 1 : 0;
        nflags |= ParameterFlags.isOut(flags) ? 2 : 0;
        nflags |= ParameterFlags.isPinned(flags) ? 8 : 0;
        return nflags |= ParameterFlags.isNulTerminate(flags) || ParameterFlags.isIn(flags) ? 4 : 0;
    }

    static int getNativeArrayFlags(Collection<Annotation> annotations) {
        return AsmUtil.getNativeArrayFlags(ParameterFlags.parse(annotations));
    }

    static LocalVariable[] getParameterVariables(ParameterType[] parameterTypes) {
        LocalVariable[] lvars = new LocalVariable[parameterTypes.length];
        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            lvars[i] = new LocalVariable(parameterTypes[i].getDeclaredType(), lvar);
            lvar += AsmUtil.calculateLocalVariableSpace((SigType)parameterTypes[i]);
        }
        return lvars;
    }

    static LocalVariable[] getParameterVariables(Class[] parameterTypes) {
        LocalVariable[] lvars = new LocalVariable[parameterTypes.length];
        int idx = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            lvars[i] = new LocalVariable(parameterTypes[i], idx);
            idx += AsmUtil.calculateLocalVariableSpace(parameterTypes[i]);
        }
        return lvars;
    }

    static void load(SkinnyMethodAdapter mv, Class parameterType, LocalVariable parameter) {
        if (!parameterType.isPrimitive()) {
            mv.aload(parameter);
        } else if (Long.TYPE == parameterType) {
            mv.lload(parameter);
        } else if (Float.TYPE == parameterType) {
            mv.fload(parameter);
        } else if (Double.TYPE == parameterType) {
            mv.dload(parameter);
        } else {
            mv.iload(parameter);
        }
    }

    static void store(SkinnyMethodAdapter mv, Class type, LocalVariable var) {
        if (!type.isPrimitive()) {
            mv.astore(var);
        } else if (Long.TYPE == type) {
            mv.lstore(var);
        } else if (Double.TYPE == type) {
            mv.dstore(var);
        } else if (Float.TYPE == type) {
            mv.fstore(var);
        } else {
            mv.istore(var);
        }
    }

    static void emitReturn(AsmBuilder builder, SkinnyMethodAdapter mv, Class returnType, Class nativeIntType) {
        if (returnType.isPrimitive()) {
            if (Long.TYPE == returnType) {
                mv.lreturn();
            } else if (Float.TYPE == returnType) {
                mv.freturn();
            } else if (Double.TYPE == returnType) {
                mv.dreturn();
            } else if (Void.TYPE == returnType) {
                mv.voidreturn();
            } else {
                mv.ireturn();
            }
        } else {
            AsmUtil.boxValue(builder, mv, returnType, nativeIntType);
            mv.areturn();
        }
    }

    static void getfield(SkinnyMethodAdapter mv, AsmBuilder builder, AsmBuilder.ObjectField field) {
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), field.name, CodegenUtils.ci(field.klass));
    }

    static void tryfinally(SkinnyMethodAdapter mv, Runnable codeBlock, Runnable finallyBlock) {
        Label before = new Label();
        Label after = new Label();
        Label ensure = new Label();
        Label done = new Label();
        mv.trycatch(before, after, ensure, null);
        mv.label(before);
        codeBlock.run();
        mv.label(after);
        if (finallyBlock != null) {
            finallyBlock.run();
        }
        mv.go_to(done);
        if (finallyBlock != null) {
            mv.label(ensure);
            finallyBlock.run();
            mv.athrow();
        }
        mv.label(done);
    }

    static void emitToNativeConversion(AsmBuilder builder, SkinnyMethodAdapter mv, ToNativeType toNativeType) {
        ToNativeConverter parameterConverter = toNativeType.getToNativeConverter();
        if (parameterConverter != null) {
            Method toNativeMethod = AsmUtil.getToNativeMethod(toNativeType, builder.getClassLoader());
            if (toNativeType.getDeclaredType().isPrimitive()) {
                AsmUtil.boxValue(builder, mv, NumberUtil.getBoxedClass(toNativeType.getDeclaredType()), toNativeType.getDeclaredType());
            }
            if (!toNativeMethod.getParameterTypes()[0].isAssignableFrom(NumberUtil.getBoxedClass(toNativeType.getDeclaredType()))) {
                mv.checkcast(toNativeMethod.getParameterTypes()[0]);
            }
            mv.aload(0);
            AsmBuilder.ObjectField toNativeConverterField = builder.getToNativeConverterField(parameterConverter);
            mv.getfield(builder.getClassNamePath(), toNativeConverterField.name, CodegenUtils.ci(toNativeConverterField.klass));
            if (!toNativeMethod.getDeclaringClass().equals(toNativeConverterField.klass)) {
                mv.checkcast(toNativeMethod.getDeclaringClass());
            }
            mv.swap();
            if (toNativeType.getToNativeContext() != null) {
                AsmUtil.getfield(mv, builder, builder.getToNativeContextField(toNativeType.getToNativeContext()));
            } else {
                mv.aconst_null();
            }
            if (toNativeMethod.getDeclaringClass().isInterface()) {
                mv.invokeinterface(toNativeMethod.getDeclaringClass(), toNativeMethod.getName(), toNativeMethod.getReturnType(), toNativeMethod.getParameterTypes());
            } else {
                mv.invokevirtual(toNativeMethod.getDeclaringClass(), toNativeMethod.getName(), toNativeMethod.getReturnType(), toNativeMethod.getParameterTypes());
            }
            if (!parameterConverter.nativeType().isAssignableFrom(toNativeMethod.getReturnType())) {
                mv.checkcast(CodegenUtils.p(parameterConverter.nativeType()));
            }
        }
    }

    static void emitFromNativeConversion(AsmBuilder builder, SkinnyMethodAdapter mv, FromNativeType fromNativeType, Class nativeClass) {
        FromNativeConverter fromNativeConverter = fromNativeType.getFromNativeConverter();
        if (fromNativeConverter != null) {
            NumberUtil.convertPrimitive(mv, nativeClass, AsmUtil.unboxedType(fromNativeConverter.nativeType()), fromNativeType.getNativeType());
            AsmUtil.boxValue(builder, mv, fromNativeConverter.nativeType(), nativeClass);
            Method fromNativeMethod = AsmUtil.getFromNativeMethod(fromNativeType, builder.getClassLoader());
            AsmUtil.getfield(mv, builder, builder.getFromNativeConverterField(fromNativeConverter));
            mv.swap();
            if (fromNativeType.getFromNativeContext() != null) {
                AsmUtil.getfield(mv, builder, builder.getFromNativeContextField(fromNativeType.getFromNativeContext()));
            } else {
                mv.aconst_null();
            }
            if (fromNativeMethod.getDeclaringClass().isInterface()) {
                mv.invokeinterface(fromNativeMethod.getDeclaringClass(), fromNativeMethod.getName(), fromNativeMethod.getReturnType(), fromNativeMethod.getParameterTypes());
            } else {
                mv.invokevirtual(fromNativeMethod.getDeclaringClass(), fromNativeMethod.getName(), fromNativeMethod.getReturnType(), fromNativeMethod.getParameterTypes());
            }
            if (fromNativeType.getDeclaredType().isPrimitive()) {
                Class boxedType = NumberUtil.getBoxedClass(fromNativeType.getDeclaredType());
                if (!boxedType.isAssignableFrom(fromNativeMethod.getReturnType())) {
                    mv.checkcast(CodegenUtils.p(boxedType));
                }
                AsmUtil.unboxNumber(mv, boxedType, fromNativeType.getDeclaredType(), fromNativeType.getNativeType());
            } else if (!fromNativeType.getDeclaredType().isAssignableFrom(fromNativeMethod.getReturnType())) {
                mv.checkcast(CodegenUtils.p(fromNativeType.getDeclaredType()));
            }
        } else if (!fromNativeType.getDeclaredType().isPrimitive()) {
            Class unboxedType = AsmUtil.unboxedType(fromNativeType.getDeclaredType());
            NumberUtil.convertPrimitive(mv, nativeClass, unboxedType, fromNativeType.getNativeType());
            AsmUtil.boxValue(builder, mv, fromNativeType.getDeclaredType(), unboxedType);
        }
    }

    static Method getToNativeMethod(ToNativeType toNativeType, AsmClassLoader classLoader) {
        ToNativeConverter toNativeConverter = toNativeType.getToNativeConverter();
        if (toNativeConverter == null) {
            return null;
        }
        try {
            Method method;
            Class<?> toNativeConverterClass = toNativeConverter.getClass();
            if (Modifier.isPublic(toNativeConverterClass.getModifiers())) {
                for (Method method2 : toNativeConverterClass.getMethods()) {
                    if (!method2.getName().equals("toNative")) continue;
                    Class<?>[] methodParameterTypes = method2.getParameterTypes();
                    if (!toNativeConverter.nativeType().isAssignableFrom(method2.getReturnType()) || methodParameterTypes.length != 2 || !methodParameterTypes[0].isAssignableFrom(toNativeType.getDeclaredType()) || methodParameterTypes[1] != ToNativeContext.class || !AsmUtil.methodIsAccessible(method2) || !AsmUtil.classIsVisible(classLoader, method2.getDeclaringClass())) continue;
                    return method2;
                }
            }
            return AsmUtil.methodIsAccessible(method = toNativeConverterClass.getMethod("toNative", Object.class, ToNativeContext.class)) && AsmUtil.classIsVisible(classLoader, method.getDeclaringClass()) ? method : ToNativeConverter.class.getDeclaredMethod("toNative", Object.class, ToNativeContext.class);
        }
        catch (NoSuchMethodException nsme) {
            try {
                return ToNativeConverter.class.getDeclaredMethod("toNative", Object.class, ToNativeContext.class);
            }
            catch (NoSuchMethodException nsme2) {
                throw new RuntimeException("internal error. " + ToNativeConverter.class + " has no toNative() method");
            }
        }
    }

    static Method getFromNativeMethod(FromNativeType fromNativeType, AsmClassLoader classLoader) {
        FromNativeConverter fromNativeConverter = fromNativeType.getFromNativeConverter();
        if (fromNativeConverter == null) {
            return null;
        }
        try {
            Method method;
            Class<?> fromNativeConverterClass = fromNativeConverter.getClass();
            if (Modifier.isPublic(fromNativeConverterClass.getModifiers())) {
                for (Method method2 : fromNativeConverterClass.getMethods()) {
                    Class javaType;
                    if (!method2.getName().equals("fromNative")) continue;
                    Class<?>[] methodParameterTypes = method2.getParameterTypes();
                    Class clazz = javaType = fromNativeType.getDeclaredType().isPrimitive() ? AsmUtil.boxedType(fromNativeType.getDeclaredType()) : fromNativeType.getDeclaredType();
                    if (!javaType.isAssignableFrom(method2.getReturnType()) || methodParameterTypes.length != 2 || !methodParameterTypes[0].isAssignableFrom(fromNativeConverter.nativeType()) || methodParameterTypes[1] != FromNativeContext.class || !AsmUtil.methodIsAccessible(method2) || !AsmUtil.classIsVisible(classLoader, method2.getDeclaringClass())) continue;
                    return method2;
                }
            }
            return AsmUtil.methodIsAccessible(method = fromNativeConverterClass.getMethod("fromNative", Object.class, FromNativeContext.class)) && AsmUtil.classIsVisible(classLoader, method.getDeclaringClass()) ? method : FromNativeConverter.class.getDeclaredMethod("fromNative", Object.class, FromNativeContext.class);
        }
        catch (NoSuchMethodException nsme) {
            try {
                return FromNativeConverter.class.getDeclaredMethod("fromNative", Object.class, FromNativeContext.class);
            }
            catch (NoSuchMethodException nsme2) {
                throw new RuntimeException("internal error. " + FromNativeConverter.class + " has no fromNative() method");
            }
        }
    }

    static boolean methodIsAccessible(Method method) {
        return Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(method.getDeclaringClass().getModifiers());
    }

    private static boolean classIsVisible(ClassLoader classLoader, Class klass) {
        try {
            return classLoader.loadClass(klass.getName()) == klass;
        }
        catch (ClassNotFoundException cnfe) {
            return false;
        }
    }
}

