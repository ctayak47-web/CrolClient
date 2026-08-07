
package jnr.ffi.byref;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

public interface ByReference<T> {
    public int nativeSize(Runtime var1);

    public void toNative(Runtime var1, Pointer var2, long var3);

    public void fromNative(Runtime var1, Pointer var2, long var3);

    public T getValue();
}

