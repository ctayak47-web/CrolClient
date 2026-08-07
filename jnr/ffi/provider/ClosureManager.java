
package jnr.ffi.provider;

import jnr.ffi.Pointer;

public interface ClosureManager {
    public <T> T newClosure(Class<? extends T> var1, T var2);

    public <T> Pointer getClosurePointer(Class<? extends T> var1, T var2);
}

