
package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Invoker;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import jnr.ffi.CallingConvention;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.SigType;
import jnr.ffi.provider.jffi.AbstractFastNumericMethodGenerator;
import jnr.ffi.provider.jffi.CodegenUtils;
import jnr.ffi.provider.jffi.FastIntMethodGenerator;
import jnr.ffi.provider.jffi.Util;

class FastNumericMethodGenerator
extends AbstractFastNumericMethodGenerator {
    private static final boolean ENABLED = Util.getBooleanProperty("jnr.ffi.fast-numeric.enabled", true);
    private static final int MAX_PARAMETERS = FastNumericMethodGenerator.getMaximumParameters();
    private static final String[] signatures;
    private static final String[] methodNames;

    FastNumericMethodGenerator() {
    }

    @Override
    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        int parameterCount = parameterTypes.length;
        if (!ENABLED) {
            return false;
        }
        if (callingConvention != CallingConvention.DEFAULT || parameterCount > MAX_PARAMETERS) {
            return false;
        }
        Platform platform = Platform.getPlatform();
        if (platform.getCPU() != Platform.CPU.I386 && platform.getCPU() != Platform.CPU.X86_64) {
            return false;
        }
        if (platform.getOS().equals((Object)Platform.OS.WINDOWS)) {
            return false;
        }
        for (ParameterType parameterType : parameterTypes) {
            if (FastNumericMethodGenerator.isFastNumericParameter(platform, parameterType)) continue;
            return false;
        }
        return FastNumericMethodGenerator.isFastNumericResult(platform, resultType);
    }

    @Override
    String getInvokerMethodName(ResultType resultType, ParameterType[] parameterTypes, boolean ignoreErrno) {
        int parameterCount = parameterTypes.length;
        if (parameterCount <= MAX_PARAMETERS && parameterCount <= methodNames.length) {
            return methodNames[parameterCount];
        }
        throw new IllegalArgumentException("invalid fast-numeric parameter count: " + parameterCount);
    }

    @Override
    String getInvokerSignature(int parameterCount, Class nativeIntType) {
        if (parameterCount <= MAX_PARAMETERS && parameterCount <= signatures.length) {
            return signatures[parameterCount];
        }
        throw new IllegalArgumentException("invalid fast-numeric parameter count: " + parameterCount);
    }

    @Override
    Class getInvokerType() {
        return Long.TYPE;
    }

    private static boolean isNumericType(Platform platform, SigType type) {
        return FastIntMethodGenerator.isFastIntType(platform, type) || type.getNativeType() == NativeType.SLONG || type.getNativeType() == NativeType.ULONG || type.getNativeType() == NativeType.SLONGLONG || type.getNativeType() == NativeType.ULONGLONG || type.getNativeType() == NativeType.FLOAT || type.getNativeType() == NativeType.DOUBLE;
    }

    static boolean isFastNumericResult(Platform platform, ResultType type) {
        return FastNumericMethodGenerator.isNumericType(platform, type) || NativeType.VOID == type.getNativeType() || NativeType.ADDRESS == type.getNativeType();
    }

    static boolean isFastNumericParameter(Platform platform, ParameterType parameterType) {
        return FastNumericMethodGenerator.isNumericType(platform, parameterType) || parameterType.getNativeType() == NativeType.ADDRESS && FastNumericMethodGenerator.isSupportedPointerParameterType(parameterType.effectiveJavaType());
    }

    private static boolean isSupportedPointerParameterType(Class javaParameterType) {
        return Pointer.class.isAssignableFrom(javaParameterType) || ByteBuffer.class.isAssignableFrom(javaParameterType) || ShortBuffer.class.isAssignableFrom(javaParameterType) || IntBuffer.class.isAssignableFrom(javaParameterType) || LongBuffer.class.isAssignableFrom(javaParameterType) && Type.SLONG.size() == 8 || FloatBuffer.class.isAssignableFrom(javaParameterType) || DoubleBuffer.class.isAssignableFrom(javaParameterType) || byte[].class == javaParameterType || short[].class == javaParameterType || int[].class == javaParameterType || long[].class == javaParameterType && Type.SLONG.size() == 8 || float[].class == javaParameterType || double[].class == javaParameterType || boolean[].class == javaParameterType;
    }

    static int getMaximumParameters() {
        try {
            Invoker.class.getDeclaredMethod("invokeN6", CallContext.class, Long.TYPE, Long.TYPE, Long.TYPE, Long.TYPE, Long.TYPE, Long.TYPE, Long.TYPE);
            return 6;
        }
        catch (Throwable t) {
            return 0;
        }
    }

    static {
        methodNames = new String[]{"invokeN0", "invokeN1", "invokeN2", "invokeN3", "invokeN4", "invokeN5", "invokeN6"};
        signatures = new String[MAX_PARAMETERS + 1];
        for (int i = 0; i <= MAX_PARAMETERS; ++i) {
            StringBuilder sb = new StringBuilder();
            sb.append('(').append(CodegenUtils.ci(CallContext.class)).append(CodegenUtils.ci(Long.TYPE));
            for (int n = 0; n < i; ++n) {
                sb.append('J');
            }
            FastNumericMethodGenerator.signatures[i] = sb.append(")J").toString();
        }
    }
}

