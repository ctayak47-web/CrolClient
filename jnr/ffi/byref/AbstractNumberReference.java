
package jnr.ffi.byref;

import jnr.ffi.byref.ByReference;

public abstract class AbstractNumberReference<T extends Number>
extends Number
implements ByReference<T> {
    T value;

    protected AbstractNumberReference(T value) {
        this.value = value;
    }

    protected static <T extends Number> T checkNull(T value) {
        if (value == null) {
            throw new NullPointerException("reference value cannot be null");
        }
        return value;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public final byte byteValue() {
        return ((Number)this.value).byteValue();
    }

    @Override
    public final short shortValue() {
        return ((Number)this.value).byteValue();
    }

    @Override
    public final int intValue() {
        return ((Number)this.value).intValue();
    }

    @Override
    public final long longValue() {
        return ((Number)this.value).longValue();
    }

    @Override
    public final float floatValue() {
        return ((Number)this.value).floatValue();
    }

    @Override
    public final double doubleValue() {
        return ((Number)this.value).doubleValue();
    }
}

