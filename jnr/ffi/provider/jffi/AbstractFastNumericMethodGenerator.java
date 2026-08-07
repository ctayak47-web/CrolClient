
package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Invoker;
import com.kenai.jffi.ObjectParameterInfo;
import com.kenai.jffi.ObjectParameterStrategy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import jnr.ffi.Pointer;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.jffi.AsmBuilder;
import jnr.ffi.provider.jffi.AsmRuntime;
import jnr.ffi.provider.jffi.AsmUtil;
import jnr.ffi.provider.jffi.BaseMethodGenerator;
import jnr.ffi.provider.jffi.BufferParameterStrategy;
import jnr.ffi.provider.jffi.CodegenUtils;
import jnr.ffi.provider.jffi.LocalVariable;
import jnr.ffi.provider.jffi.LocalVariableAllocator;
import jnr.ffi.provider.jffi.NumberUtil;
import jnr.ffi.provider.jffi.ParameterStrategy;
import jnr.ffi.provider.jffi.PointerParameterStrategy;
import jnr.ffi.provider.jffi.SkinnyMethodAdapter;
import jnr.ffi.provider.jffi.ToNativeOp;
import org.objectweb.asm.Label;

abstract class AbstractFastNumericMethodGenerator
extends BaseMethodGenerator {
    static final Map<Class<? extends ObjectParameterStrategy>, Method> STRATEGY_ADDRESS_METHODS;
    static final Map<Class, Class<? extends ObjectParameterStrategy>> STRATEGY_PARAMETER_TYPES;

    AbstractFastNumericMethodGenerator() {
    }

    @Override
    public void generate(AsmBuilder builder, SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, CallContext callContext, ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError) {
        Class<Float> nativeIntType = this.getInvokerType();
        LocalVariable objCount = localVariableAllocator.allocate(Integer.TYPE);
        LocalVariable[] parameters = AsmUtil.getParameterVariables(parameterTypes);
        LocalVariable[] converted = new LocalVariable[parameterTypes.length];
        int pointerCount = 0;
        for (int i = 0; i < parameterTypes.length; ++i) {
            converted[i] = AbstractFastNumericMethodGenerator.loadAndConvertParameter(builder, mv, localVariableAllocator, parameters[i], parameterTypes[i]);
            Class javaParameterType = parameterTypes[i].effectiveJavaType();
            ToNativeOp op = ToNativeOp.get(parameterTypes[i]);
            if (op != null && op.isPrimitive()) {
                op.emitPrimitive(mv, this.getInvokerType(), parameterTypes[i].getNativeType());
                continue;
            }
            if (AbstractFastNumericMethodGenerator.hasPointerParameterStrategy(javaParameterType)) {
                pointerCount = AbstractFastNumericMethodGenerator.emitDirectCheck(mv, javaParameterType, nativeIntType, converted[i], objCount, pointerCount);
                continue;
            }
            throw new IllegalArgumentException("unsupported parameter type " + parameterTypes[i].getDeclaredType());
        }
        Label hasObjects = new Label();
        Label convertResult = new Label();
        if (pointerCount > 0) {
            mv.iload(objCount);
            mv.ifne(hasObjects);
        }
        mv.invokevirtual(CodegenUtils.p(Invoker.class), this.getInvokerMethodName(resultType, parameterTypes, ignoreError), this.getInvokerSignature(parameterTypes.length, nativeIntType));
        if (pointerCount > 0) {
            mv.label(convertResult);
        }
        Class javaReturnType = resultType.effectiveJavaType();
        Class<Number> nativeReturnType = nativeIntType;
        if (Float.class == javaReturnType || Float.TYPE == javaReturnType) {
            NumberUtil.narrow(mv, nativeIntType, Integer.TYPE);
            mv.invokestatic(Float.class, "intBitsToFloat", Float.TYPE, Integer.TYPE);
            nativeReturnType = Float.TYPE;
        } else if (Double.class == javaReturnType || Double.TYPE == javaReturnType) {
            NumberUtil.widen(mv, nativeIntType, Long.TYPE);
            mv.invokestatic(Double.class, "longBitsToDouble", Double.TYPE, Long.TYPE);
            nativeReturnType = Double.TYPE;
        }
        Class unboxedResultType = AsmUtil.unboxedReturnType(javaReturnType);
        NumberUtil.convertPrimitive(mv, nativeReturnType, unboxedResultType, resultType.getNativeType());
        AbstractFastNumericMethodGenerator.emitEpilogue(builder, mv, resultType, parameterTypes, parameters, converted, null);
        if (pointerCount > 0) {
            int i;
            mv.label(hasObjects);
            if (Integer.TYPE == nativeIntType) {
                LocalVariable[] tmp = new LocalVariable[parameterTypes.length];
                for (i = parameterTypes.length - 1; i > 0; --i) {
                    tmp[i] = localVariableAllocator.allocate(Integer.TYPE);
                    mv.istore(tmp[i]);
                }
                if (parameterTypes.length > 0) {
                    mv.i2l();
                }
                for (i = 1; i < parameterTypes.length; ++i) {
                    mv.iload(tmp[i]);
                    mv.i2l();
                }
            }
            mv.iload(objCount);
            LocalVariable[] strategies = new LocalVariable[parameterTypes.length];
            for (i = 0; i < parameterTypes.length; ++i) {
                Class javaParameterType = parameterTypes[i].effectiveJavaType();
                if (!AbstractFastNumericMethodGenerator.hasPointerParameterStrategy(javaParameterType)) continue;
                mv.aload(converted[i]);
                AbstractFastNumericMethodGenerator.emitParameterStrategyLookup(mv, javaParameterType);
                strategies[i] = localVariableAllocator.allocate(ParameterStrategy.class);
                mv.astore(strategies[i]);
                mv.aload(converted[i]);
                mv.aload(strategies[i]);
                mv.aload(0);
                ObjectParameterInfo info = ObjectParameterInfo.create(i, AsmUtil.getNativeArrayFlags(parameterTypes[i].annotations()));
                mv.getfield(builder.getClassNamePath(), builder.getObjectParameterInfoName(info), CodegenUtils.ci(ObjectParameterInfo.class));
            }
            mv.invokevirtual(CodegenUtils.p(Invoker.class), AbstractFastNumericMethodGenerator.getObjectParameterMethodName(parameterTypes.length), AbstractFastNumericMethodGenerator.getObjectParameterMethodSignature(parameterTypes.length, pointerCount));
            NumberUtil.narrow(mv, Long.TYPE, nativeIntType);
            mv.go_to(convertResult);
        }
    }

    private static void addStrategyParameterType(Map<Class<? extends ObjectParameterStrategy>, Method> map, Class<? extends ObjectParameterStrategy> strategyClass, Class parameterType) {
        try {
            Method addressMethod = strategyClass.getDeclaredMethod("address", parameterType);
            if (Modifier.isPublic(addressMethod.getModifiers()) && Modifier.isPublic(addressMethod.getDeclaringClass().getModifiers())) {
                map.put(strategyClass, addressMethod);
            }
        }
        catch (NoSuchMethodException noSuchMethodException) {
            
        }
    }

    static boolean hasPointerParameterStrategy(Class javaType) {
        for (Class c : STRATEGY_PARAMETER_TYPES.keySet()) {
            if (!c.isAssignableFrom(javaType)) continue;
            return true;
        }
        return false;
    }

    static Class<? extends ObjectParameterStrategy> emitParameterStrategyLookup(SkinnyMethodAdapter mv, Class javaParameterType) {
        for (Map.Entry<Class, Class<? extends ObjectParameterStrategy>> e : STRATEGY_PARAMETER_TYPES.entrySet()) {
            if (!e.getKey().isAssignableFrom(javaParameterType)) continue;
            mv.invokestatic(AsmRuntime.class, "pointerParameterStrategy", e.getValue(), e.getKey());
            return e.getValue();
        }
        throw new RuntimeException("no conversion strategy for: " + javaParameterType);
    }

    static void emitParameterStrategyAddress(SkinnyMethodAdapter mv, Class nativeIntType, Class<? extends ObjectParameterStrategy> strategyClass, LocalVariable strategy, LocalVariable parameter) {
        mv.aload(strategy);
        mv.aload(parameter);
        Method addressMethod = STRATEGY_ADDRESS_METHODS.get(strategyClass);
        if (addressMethod != null) {
            mv.invokevirtual(strategyClass, addressMethod.getName(), addressMethod.getReturnType(), addressMethod.getParameterTypes());
        } else {
            mv.invokevirtual(PointerParameterStrategy.class, "address", Long.TYPE, Object.class);
        }
        NumberUtil.narrow(mv, Long.TYPE, nativeIntType);
    }

    static int emitDirectCheck(SkinnyMethodAdapter mv, Class javaParameterClass, Class nativeIntType, LocalVariable parameter, LocalVariable objCount, int pointerCount) {
        if (pointerCount < 1) {
            mv.iconst_0();
            mv.istore(objCount);
        }
        Label next = new Label();
        Label nullPointer = new Label();
        mv.ifnull(nullPointer);
        if (Pointer.class.isAssignableFrom(javaParameterClass)) {
            mv.aload(parameter);
            mv.invokevirtual(Pointer.class, "address", Long.TYPE, new Class[0]);
            NumberUtil.narrow(mv, Long.TYPE, nativeIntType);
            mv.aload(parameter);
            mv.invokevirtual(Pointer.class, "isDirect", Boolean.TYPE, new Class[0]);
            mv.iftrue(next);
        } else if (Buffer.class.isAssignableFrom(javaParameterClass)) {
            mv.aload(parameter);
            mv.invokestatic(BufferParameterStrategy.class, "address", Long.TYPE, Buffer.class);
            NumberUtil.narrow(mv, Long.TYPE, nativeIntType);
            mv.aload(parameter);
            mv.invokevirtual(Buffer.class, "isDirect", Boolean.TYPE, new Class[0]);
            mv.iftrue(next);
        } else if (javaParameterClass.isArray() && javaParameterClass.getComponentType().isPrimitive()) {
            if (Long.TYPE == nativeIntType) {
                mv.lconst_0();
            } else {
                mv.iconst_0();
            }
        } else {
            throw new UnsupportedOperationException("unsupported parameter type: " + javaParameterClass);
        }
        mv.iinc(objCount, 1);
        mv.go_to(next);
        mv.label(nullPointer);
        if (Long.TYPE == nativeIntType) {
            mv.lconst_0();
        } else {
            mv.iconst_0();
        }
        mv.label(next);
        return ++pointerCount;
    }

    static String getObjectParameterMethodName(int parameterCount) {
        return "invokeN" + parameterCount;
    }

    static String getObjectParameterMethodSignature(int parameterCount, int pointerCount) {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(CodegenUtils.ci(CallContext.class)).append(CodegenUtils.ci(Long.TYPE));
        for (int i = 0; i < parameterCount; ++i) {
            sb.append('J');
        }
        sb.append('I');
        for (int n = 0; n < pointerCount; ++n) {
            sb.append(CodegenUtils.ci(Object.class));
            sb.append(CodegenUtils.ci(ObjectParameterStrategy.class));
            sb.append(CodegenUtils.ci(ObjectParameterInfo.class));
        }
        sb.append(")J");
        return sb.toString();
    }

    abstract String getInvokerMethodName(ResultType var1, ParameterType[] var2, boolean var3);

    abstract String getInvokerSignature(int var1, Class var2);

    abstract Class getInvokerType();

    static {
        HashMap<Class<? extends ObjectParameterStrategy>, Method> strategies = new HashMap<Class<? extends ObjectParameterStrategy>, Method>();
        AbstractFastNumericMethodGenerator.addStrategyParameterType(strategies, BufferParameterStrategy.class, Buffer.class);
        AbstractFastNumericMethodGenerator.addStrategyParameterType(strategies, PointerParameterStrategy.class, Pointer.class);
        STRATEGY_ADDRESS_METHODS = Collections.unmodifiableMap(strategies);
        LinkedHashMap<Class, Class<ParameterStrategy>> types = new LinkedHashMap<Class, Class<ParameterStrategy>>();
        types.put(Pointer.class, PointerParameterStrategy.class);
        for (Class c : new Class[]{ByteBuffer.class, CharBuffer.class, ShortBuffer.class, IntBuffer.class, LongBuffer.class, FloatBuffer.class, DoubleBuffer.class, Buffer.class}) {
            types.put(c, BufferParameterStrategy.class);
        }
        for (Class c : new Class[]{byte[].class, short[].class, char[].class, int[].class, long[].class, float[].class, double[].class, boolean[].class}) {
            types.put(c, ParameterStrategy.class);
        }
        STRATEGY_PARAMETER_TYPES = Collections.unmodifiableMap(types);
    }
}

