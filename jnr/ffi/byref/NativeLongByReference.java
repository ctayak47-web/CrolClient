
package jnr.ffi.byref;

import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.byref.AbstractNumberReference;

public final class NativeLongByReference
extends AbstractNumberReference<NativeLong> {
    public NativeLongByReference() {
        super(NativeLong.valueOf(0));
    }

    public NativeLongByReference(NativeLong value) {
        super(NativeLongByReference.checkNull(value));
    }

    public NativeLongByReference(long value) {
        super(NativeLong.valueOf(value));
    }

    @Override
    public void toNative(Runtime runtime, Pointer memory, long offset) {
        memory.putNativeLong(offset, ((NativeLong)this.value).longValue());
    }

    @Override
    public void fromNative(Runtime runtime, Pointer memory, long offset) {
        this.value = NativeLong.valueOf(memory.getNativeLong(offset));
    }

    @Override
    public final int nativeSize(Runtime runtime) {
        return runtime.longSize();
    }
}

