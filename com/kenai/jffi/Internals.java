
package com.kenai.jffi;

import com.kenai.jffi.Foreign;

public final class Internals {
    private Internals() {
    }

    public static final long getErrnoSaveFunction() {
        return Foreign.getInstance().getSaveErrnoFunction();
    }
}

