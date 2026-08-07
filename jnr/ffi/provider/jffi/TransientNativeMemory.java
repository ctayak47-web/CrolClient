
package jnr.ffi.provider.jffi;

import com.kenai.jffi.PageManager;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jnr.ffi.Runtime;
import jnr.ffi.provider.jffi.AllocatedDirectMemoryIO;
import jnr.ffi.provider.jffi.DirectMemoryIO;
import jnr.ffi.provider.jffi.NativeFinalizer;
import jnr.ffi.util.ref.FinalizablePhantomReference;
import jnr.ffi.util.ref.FinalizableReferenceQueue;

public class TransientNativeMemory
extends DirectMemoryIO {
    private static final Map<Magazine, Boolean> referenceSet = new ConcurrentHashMap<Magazine, Boolean>();
    private static final ThreadLocal<Magazine> currentMagazine = new ThreadLocal();
    private static final int PAGES_PER_MAGAZINE = 2;
    private final Sentinel sentinel;
    private final long size;

    public static DirectMemoryIO allocate(Runtime runtime, int size, int align, boolean clear) {
        return TransientNativeMemory.allocate(runtime, (long)size, align, clear);
    }

    public static DirectMemoryIO allocate(Runtime runtime, long size, int align, boolean clear) {
        long address;
        Sentinel sentinel;
        if (size < 0L) {
            throw new IllegalArgumentException("negative size: " + size);
        }
        if (size > 256L) {
            return new AllocatedDirectMemoryIO(runtime, size, clear);
        }
        Magazine magazine = currentMagazine.get();
        Sentinel sentinel2 = sentinel = magazine != null ? magazine.sentinel() : null;
        if (sentinel == null || (address = magazine.allocate(size, align)) == 0L) {
            long memory;
            PageManager pm = PageManager.getInstance();
            while ((memory = pm.allocatePages(2, 3)) == 0L || memory == -1L) {
                System.gc();
                FinalizableReferenceQueue.cleanUpAll();
            }
            sentinel = new Sentinel();
            magazine = new Magazine(sentinel, pm, memory, 2);
            referenceSet.put(magazine, Boolean.TRUE);
            currentMagazine.set(magazine);
            address = magazine.allocate(size, align);
        }
        return new TransientNativeMemory(runtime, sentinel, address, size);
    }

    TransientNativeMemory(Runtime runtime, Sentinel sentinel, long address, long size) {
        super(runtime, address);
        this.sentinel = sentinel;
        this.size = size;
    }

    private static long align(long offset, long align) {
        return offset + align - 1L & (align - 1L ^ 0xFFFFFFFFFFFFFFFFL);
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
        if (obj instanceof TransientNativeMemory) {
            TransientNativeMemory mem = (TransientNativeMemory)obj;
            return mem.size == this.size && mem.address() == this.address();
        }
        return super.equals(obj);
    }

    public final void dispose() {
    }

    private static final class Magazine
    extends FinalizablePhantomReference<Sentinel> {
        private final Reference<Sentinel> sentinelReference;
        private final PageManager pm;
        private final long page;
        private final long end;
        private final int pageCount;
        private long memory;

        Magazine(Sentinel sentinel, PageManager pm, long page, int pageCount) {
            super(sentinel, NativeFinalizer.getInstance().getFinalizerQueue());
            this.sentinelReference = new WeakReference<Sentinel>(sentinel);
            this.pm = pm;
            this.memory = this.page = page;
            this.pageCount = pageCount;
            this.end = this.memory + (long)pageCount * pm.pageSize();
        }

        Sentinel sentinel() {
            return this.sentinelReference.get();
        }

        long allocate(long size, int align) {
            long address = TransientNativeMemory.align(this.memory, align);
            if (address + size <= this.end) {
                this.memory = address + size;
                return address;
            }
            return 0L;
        }

        @Override
        public final void finalizeReferent() {
            this.pm.freePages(this.page, this.pageCount);
            referenceSet.remove(this);
        }
    }

    private static final class Sentinel {
        private Sentinel() {
        }
    }
}

