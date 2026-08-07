
package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Invoker;
import com.kenai.jffi.Platform;
import jnr.ffi.CallingConvention;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.SigType;
import jnr.ffi.provider.jffi.AbstractFastNumericMethodGenerator;
import jnr.ffi.provider.jffi.CodegenUtils;
import jnr.ffi.provider.jffi.NumberUtil;
import jnr.ffi.provider.jffi.Util;

final class FastIntMethodGenerator
extends AbstractFastNumericMethodGenerator {
    private static final boolean ENABLED = Util.getBooleanProperty("jnr.ffi.fast-int.enabled", true);
    private static final int MAX_FASTINT_PARAMETERS = FastIntMethodGenerator.getMaximumFastIntParameters();
    private static final String[] signatures;
    private static final String[] methodNames;

    FastIntMethodGenerator() {
    }

    @Override
    String getInvokerMethodName(ResultType resultType, ParameterType[] parameterTypes, boolean ignoreErrno) {
        int parameterCount = parameterTypes.length;
        if (parameterCount <= MAX_FASTINT_PARAMETERS && parameterCount <= methodNames.length) {
            return methodNames[parameterCount];
        }
        throw new IllegalArgumentException("invalid fast-int parameter count: " + parameterCount);
    }

    @Override
    String getInvokerSignature(int parameterCount, Class nativeIntType) {
        if (parameterCount <= MAX_FASTINT_PARAMETERS && parameterCount <= signatures.length) {
            return signatures[parameterCount];
        }
        throw new IllegalArgumentException("invalid fast-int parameter count: " + parameterCount);
    }

    @Override
    final Class getInvokerType() {
        return Integer.TYPE;
    }

    @Override
    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        int parameterCount = parameterTypes.length;
        if (!ENABLED) {
            return false;
        }
        if (!callingConvention.equals((Object)CallingConvention.DEFAULT) || parameterCount > MAX_FASTINT_PARAMETERS) {
            return false;
        }
        Platform platform = Platform.getPlatform();
        if (platform.getOS().equals((Object)Platform.OS.WINDOWS)) {
            return false;
        }
        if (!platform.getCPU().equals((Object)Platform.CPU.I386) && !platform.getCPU().equals((Object)Platform.CPU.X86_64)) {
            return false;
        }
        for (ParameterType parameterType : parameterTypes) {
            if (FastIntMethodGenerator.isFastIntParameter(platform, parameterType)) continue;
            return false;
        }
        return FastIntMethodGenerator.isFastIntResult(platform, resultType);
    }

    static int getMaximumFastIntParameters() {
        try {
            Invoker.class.getDeclaredMethod("invokeI6", CallContext.class, Long.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            return 6;
        }
        catch (Throwable t) {
            return 0;
        }
    }

    static boolean isFastIntType(Platform platform, SigType type) {
        switch (type.getNativeType()) {
            case SCHAR: 
            case UCHAR: 
            case SSHORT: 
            case USHORT: 
            case SINT: 
            case UINT: 
            case SLONG: 
            case ULONG: {
                return NumberUtil.sizeof(type.getNativeType()) <= 4;
            }
        }
        return false;
    }

    private static boolean isSupportedPointerParameterType(Class javaParameterType) {
        return Pointer.class.isAssignableFrom(javaParameterType);
    }

    static boolean isFastIntResult(Platform platform, ResultType resultType) {
        return FastIntMethodGenerator.isFastIntType(platform, resultType) || resultType.getNativeType() == NativeType.VOID || resultType.getNativeType() == NativeType.ADDRESS && NumberUtil.sizeof(resultType) == 4;
    }

    static boolean isFastIntParameter(Platform platform, ParameterType parameterType) {
        return FastIntMethodGenerator.isFastIntType(platform, parameterType) || parameterType.getNativeType() == NativeType.ADDRESS && NumberUtil.sizeof(parameterType) == 4 && FastIntMethodGenerator.isSupportedPointerParameterType(parameterType.effectiveJavaType());
    }

    static {
        methodNames = new String[]{"invokeI0", "invokeI1", "invokeI2", "invokeI3", "invokeI4", "invokeI5", "invokeI6"};
        signatures = new String[MAX_FASTINT_PARAMETERS + 1];
        for (int i = 0; i <= MAX_FASTINT_PARAMETERS; ++i) {
            StringBuilder sb = new StringBuilder();
            sb.append('(').append(CodegenUtils.ci(CallContext.class)).append(CodegenUtils.ci(Long.TYPE));
            for (int n = 0; n < i; ++n) {
                sb.append('I');
            }
            FastIntMethodGenerator.signatures[i] = sb.append(")I").toString();
        }
    }
}

