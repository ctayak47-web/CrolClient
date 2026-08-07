
package jnr.ffi.provider.jffi;

import java.nio.ByteBuffer;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.provider.BoundedMemoryIO;
import jnr.ffi.provider.IntPointer;
import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.jffi.ArrayMemoryIO;
import jnr.ffi.provider.jffi.ByteBufferMemoryIO;
import jnr.ffi.provider.jffi.DirectMemoryIO;
import jnr.ffi.provider.jffi.NativeRuntime;
import jnr.ffi.provider.jffi.TransientNativeMemory;

public class NativeMemoryManager
implements MemoryManager {
    private final Runtime runtime;
    private final long addressMask;

    public NativeMemoryManager(NativeRuntime runtime) {
        this.runtime = runtime;
        this.addressMask = runtime.addressMask();
    }

    @Override
    public Pointer allocate(int size) {
        return new ArrayMemoryIO(this.runtime, size);
    }

    @Override
    public Pointer allocateDirect(int size) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(this.runtime, size, 8, true), 0L, size);
    }

    @Override
    public Pointer allocateDirect(long size) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(this.runtime, size, 8, true), 0L, size);
    }

    @Override
    public Pointer allocateDirect(int size, boolean clear) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(this.runtime, size, 8, clear), 0L, size);
    }

    @Override
    public Pointer allocateDirect(long size, boolean clear) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(this.runtime, size, 8, clear), 0L, size);
    }

    public Pointer allocateTemporary(int size) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(this.runtime, size, 8, true), 0L, size);
    }

    @Override
    public Pointer allocateTemporary(int size, boolean clear) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(this.runtime, size, 8, clear), 0L, size);
    }

    @Override
    public Pointer newPointer(ByteBuffer buffer) {
        return new ByteBufferMemoryIO(this.runtime, buffer);
    }

    @Override
    public Pointer newPointer(long address) {
        return new DirectMemoryIO(this.runtime, address & this.addressMask);
    }

    @Override
    public Pointer newPointer(long address, long size) {
        return new BoundedMemoryIO(new DirectMemoryIO(this.runtime, address & this.addressMask), 0L, size);
    }

    @Override
    public Pointer newOpaquePointer(long address) {
        return new IntPointer(this.runtime, address);
    }
}

