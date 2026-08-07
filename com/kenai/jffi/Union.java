
package com.kenai.jffi;

import com.kenai.jffi.Aggregate;
import com.kenai.jffi.Foreign;
import com.kenai.jffi.Type;
import java.util.Arrays;

public final class Union
extends Aggregate {
    private final Type[] fields;

    public static Union newUnion(Type ... fields) {
        return new Union(fields);
    }

    public Union(Type ... fields) {
        super(Foreign.getInstance(), Foreign.getInstance().newStruct(Type.nativeHandles(fields), true));
        this.fields = (Type[])fields.clone();
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
        Union union = (Union)o;
        return Arrays.equals(this.fields, union.fields);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.fields != null ? Arrays.hashCode(this.fields) : 0);
        return result;
    }
}

