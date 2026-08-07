
package jnr.ffi.byref;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.byref.AbstractNumberReference;

public final class LongLongByReference
extends AbstractNumberReference<Long> {
    public LongLongByReference() {
        super(0L);
    }

    public LongLongByReference(Long value) {
        super(LongLongByReference.checkNull(value));
    }

    public LongLongByReference(long value) {
        super(value);
    }

    @Override
    public void toNative(Runtime runtime, Pointer memory, long offset) {
        memory.putLongLong(offset, (Long)this.value);
    }

    @Override
    public void fromNative(Runtime runtime, Pointer memory, long offset) {
        this.value = memory.getLongLong(offset);
    }

    @Override
    public final int nativeSize(Runtime runtime) {
        return 8;
    }
}

