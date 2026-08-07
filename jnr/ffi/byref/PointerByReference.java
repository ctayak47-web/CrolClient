
package jnr.ffi.byref;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.byref.AbstractReference;

public final class PointerByReference
extends AbstractReference<Pointer> {
    public PointerByReference() {
        super(null);
    }

    public PointerByReference(Pointer value) {
        super(value);
    }

    @Override
    public final void toNative(Runtime runtime, Pointer memory, long offset) {
        memory.putPointer(offset, (Pointer)this.value);
    }

    @Override
    public final void fromNative(Runtime runtime, Pointer memory, long offset) {
        this.value = memory.getPointer(offset);
    }

    @Override
    public final int nativeSize(Runtime runtime) {
        return runtime.addressSize();
    }
}

