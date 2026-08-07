
package jnr.posix.util;

public class MethodName {
    private static final int CLIENT_CODE_STACK_INDEX;

    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
    }

    public static String getCallerMethodName() {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX + 1].getMethodName();
    }

    static {
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            ++i;
            if (ste.getClassName().equals(MethodName.class.getName())) break;
        }
        CLIENT_CODE_STACK_INDEX = i;
    }
}

