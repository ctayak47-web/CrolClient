
package jnr.ffi.provider;

import java.util.Collection;
import java.util.Map;
import jnr.ffi.LibraryLoader;
import jnr.ffi.LibraryOption;
import jnr.ffi.Runtime;
import jnr.ffi.provider.FFIProvider;
import jnr.ffi.provider.InvalidRuntime;

final class InvalidProvider
extends FFIProvider {
    private final String message;
    private final Throwable cause;
    private final Runtime runtime;

    InvalidProvider(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
        this.runtime = new InvalidRuntime(message, cause);
    }

    @Override
    public Runtime getRuntime() {
        return this.runtime;
    }

    @Override
    public <T> LibraryLoader<T> createLibraryLoader(Class<T> interfaceClass) {
        return new LibraryLoader<T>(interfaceClass){

            @Override
            protected T loadLibrary(Class<T> interfaceClass, Collection<String> libraryNames, Collection<String> searchPaths, Map<LibraryOption, Object> options, boolean failImmediately) {
                UnsatisfiedLinkError error = new UnsatisfiedLinkError(InvalidProvider.this.message);
                error.initCause(InvalidProvider.this.cause);
                throw error;
            }
        };
    }
}

