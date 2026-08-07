
package jnr.ffi;

import jnr.ffi.NativeType;

public abstract class Type {
    public abstract int size();

    public abstract int alignment();

    public abstract NativeType getNativeType();
}

