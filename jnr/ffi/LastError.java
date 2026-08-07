
package jnr.ffi;

import jnr.ffi.Runtime;

public final class LastError {
    private LastError() {
    }

    public static int getLastError(Runtime runtime) {
        return runtime.getLastError();
    }

    public static void setLastError(Runtime runtime, int error) {
        runtime.setLastError(error);
    }
}

