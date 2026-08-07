
package jnr.ffi.provider.jffi;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Runtime;
import jnr.ffi.provider.FFIProvider;
import jnr.ffi.provider.jffi.NativeLibraryLoader;
import jnr.ffi.provider.jffi.NativeRuntime;

public final class Provider
extends FFIProvider {
    private final NativeRuntime runtime = NativeRuntime.getInstance();

    @Override
    public final Runtime getRuntime() {
        return this.runtime;
    }

    @Override
    public <T> LibraryLoader<T> createLibraryLoader(Class<T> interfaceClass) {
        return new NativeLibraryLoader<T>(interfaceClass);
    }
}

