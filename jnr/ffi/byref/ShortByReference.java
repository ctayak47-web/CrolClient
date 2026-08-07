
package jnr.ffi.byref;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.byref.AbstractNumberReference;

public final class ShortByReference
extends AbstractNumberReference<Short> {
    public ShortByReference() {
        super((short)0);
    }

    public ShortByReference(Short value) {
        super(ShortByReference.checkNull(value));
    }

    public ShortByReference(short value) {
        super(value);
    }

    @Override
    public void toNative(Runtime runtime, Pointer buffer, long offset) {
        buffer.putShort(offset, (Short)this.value);
    }

    @Override
    public void fromNative(Runtime runtime, Pointer buffer, long offset) {
        this.value = buffer.getShort(offset);
    }

    @Override
    public final int nativeSize(Runtime runtime) {
        return 2;
    }
}

