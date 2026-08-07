
package com.kenai.jffi;

import com.kenai.jffi.Aggregate;
import com.kenai.jffi.Foreign;
import com.kenai.jffi.Type;

public final class Array
extends Aggregate {
    private final Type elementType;
    private final int length;

    public static Array newArray(Type elementType, int length) {
        return new Array(elementType, length);
    }

    public Array(Type elementType, int length) {
        super(Foreign.getInstance(), Foreign.getInstance().newArray(elementType.handle(), length));
        this.elementType = elementType;
        this.length = length;
    }

    public final Type getElementType() {
        return this.elementType;
    }

    public final int length() {
        return this.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Array array = (Array)o;
        if (this.length != array.length) {
            return false;
        }
        return !(this.elementType != null ? !this.elementType.equals(array.elementType) : array.elementType != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.elementType != null ? this.elementType.hashCode() : 0);
        result = 31 * result + this.length;
        return result;
    }
}

