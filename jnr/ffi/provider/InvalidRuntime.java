
package jnr.ffi.provider;

import java.nio.ByteOrder;
import jnr.ffi.NativeType;
import jnr.ffi.ObjectReferenceManager;
import jnr.ffi.Runtime;
import jnr.ffi.Type;
import jnr.ffi.TypeAlias;
import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.MemoryManager;

class InvalidRuntime
extends Runtime {
    private final String message;
    private final Throwable cause;

    InvalidRuntime(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    @Override
    public Type findType(NativeType type) {
        throw this.newLoadError();
    }

    @Override
    public Type findType(TypeAlias type) {
        throw this.newLoadError();
    }

    @Override
    public MemoryManager getMemoryManager() {
        throw this.newLoadError();
    }

    @Override
    public ClosureManager getClosureManager() {
        throw this.newLoadError();
    }

    public ObjectReferenceManager newObjectReferenceManager() {
        throw this.newLoadError();
    }

    @Override
    public int getLastError() {
        throw this.newLoadError();
    }

    @Override
    public void setLastError(int error) {
        throw this.newLoadError();
    }

    @Override
    public long addressMask() {
        throw this.newLoadError();
    }

    @Override
    public int addressSize() {
        throw this.newLoadError();
    }

    @Override
    public int longSize() {
        throw this.newLoadError();
    }

    @Override
    public ByteOrder byteOrder() {
        throw this.newLoadError();
    }

    @Override
    public boolean isCompatible(Runtime other) {
        throw this.newLoadError();
    }

    private UnsatisfiedLinkError newLoadError() {
        UnsatisfiedLinkError error = new UnsatisfiedLinkError(this.message);
        error.initCause(this.cause);
        throw error;
    }
}

