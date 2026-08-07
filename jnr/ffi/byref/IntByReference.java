
package jnr.ffi.byref;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.byref.AbstractNumberReference;

public final class IntByReference
extends AbstractNumberReference<Integer> {
    public IntByReference() {
        super(0);
    }

    public IntByReference(Integer value) {
        super(IntByReference.checkNull(value));
    }

    public IntByReference(int value) {
        super(value);
    }

    @Override
    public void toNative(Runtime runtime, Pointer buffer, long offset) {
        buffer.putInt(offset, (Integer)this.value);
    }

    @Override
    public void fromNative(Runtime runtime, Pointer buffer, long offset) {
        this.value = buffer.getInt(offset);
    }

    @Override
    public int nativeSize(Runtime runtime) {
        return 4;
    }
}

