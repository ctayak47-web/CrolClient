
package jnr.ffi.provider.jffi;

import com.kenai.jffi.Closure;
import jnr.ffi.Runtime;
import jnr.ffi.provider.InAccessibleMemoryIO;
import jnr.ffi.provider.jffi.NativeClosureProxy;

class NativeClosurePointer
extends InAccessibleMemoryIO {
    private final Closure.Handle handle;
    final NativeClosureProxy proxy;

    public NativeClosurePointer(Runtime runtime, Closure.Handle handle, NativeClosureProxy proxy) {
        super(runtime, handle.getAddress(), true);
        this.handle = handle;
        this.proxy = proxy;
    }

    @Override
    public long size() {
        return 0L;
    }
}

