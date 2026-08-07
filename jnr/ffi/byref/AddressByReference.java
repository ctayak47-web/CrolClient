
package jnr.ffi.byref;

import jnr.ffi.Address;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.byref.AbstractReference;

public final class AddressByReference
extends AbstractReference<Address> {
    public AddressByReference() {
        super(Address.valueOf(0));
    }

    public AddressByReference(Address value) {
        super(AddressByReference.checkNull(value));
    }

    @Override
    public void toNative(Runtime runtime, Pointer memory, long offset) {
        memory.putAddress(offset, ((Address)this.value).nativeAddress());
    }

    @Override
    public void fromNative(Runtime runtime, Pointer memory, long offset) {
        this.value = Address.valueOf(memory.getAddress(offset));
    }

    @Override
    public int nativeSize(Runtime runtime) {
        return runtime.addressSize();
    }
}

