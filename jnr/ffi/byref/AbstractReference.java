
package jnr.ffi.byref;

import jnr.ffi.byref.ByReference;

public abstract class AbstractReference<T>
implements ByReference<T> {
    T value;

    protected AbstractReference(T value) {
        this.value = value;
    }

    protected static <T> T checkNull(T value) {
        if (value == null) {
            throw new NullPointerException("reference value cannot be null");
        }
        return value;
    }

    @Override
    public T getValue() {
        return this.value;
    }
}

