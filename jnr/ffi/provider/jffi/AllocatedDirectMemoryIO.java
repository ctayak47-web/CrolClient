
package jnr.ffi.provider.jffi;

import java.util.concurrent.atomic.AtomicBoolean;
import jnr.ffi.Runtime;
import jnr.ffi.provider.jffi.DirectMemoryIO;

class AllocatedDirectMemoryIO
extends DirectMemoryIO {
    private final AtomicBoolean allocated = new AtomicBoolean(true);
    private final long size;

    public AllocatedDirectMemoryIO(Runtime runtime, long size, boolean clear) {
        super(runtime, IO.allocateMemory(size, clear));
        this.size = size;
        if (this.address() == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AllocatedDirectMemoryIO) {
            AllocatedDirectMemoryIO mem = (AllocatedDirectMemoryIO)obj;
            return mem.size == this.size && mem.address() == this.address();
        }
        return super.equals(obj);
    }

    public final void dispose() {
        if (this.allocated.getAndSet(false)) {
            IO.freeMemory(this.address());
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.allocated.getAndSet(false)) {
                IO.freeMemory(this.address());
            }
        }
        finally {
            super.finalize();
        }
    }
}

