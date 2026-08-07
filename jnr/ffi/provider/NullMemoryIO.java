
package jnr.ffi.provider;

import jnr.ffi.Runtime;
import jnr.ffi.provider.InAccessibleMemoryIO;

public final class NullMemoryIO
extends InAccessibleMemoryIO {
    private static final String msg = "attempted access to a NULL memory address";

    public NullMemoryIO(Runtime runtime) {
        super(runtime, 0L, true);
    }

    @Override
    protected final NullPointerException error() {
        return new NullPointerException(msg);
    }

    @Override
    public long size() {
        return Long.MAX_VALUE;
    }
}

