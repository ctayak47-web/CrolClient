
package com.kenai.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Closure;
import com.kenai.jffi.DirectClosureBuffer;
import com.kenai.jffi.Foreign;
import com.kenai.jffi.MemoryIO;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ClosurePool {
    private final Set<Magazine> magazines = Collections.synchronizedSet(new HashSet());
    private final ConcurrentLinkedQueue<Handle> freeQueue = new ConcurrentLinkedQueue();
    private final ConcurrentLinkedQueue<Handle> partialQueue = new ConcurrentLinkedQueue();
    private final CallContext callContext;
    private static final Closure NULL_CLOSURE = new Closure(){

        @Override
        public void invoke(Closure.Buffer buffer) {
        }
    };

    ClosurePool(CallContext callContext) {
        this.callContext = callContext;
    }

    synchronized void recycle(Magazine magazine) {
        magazine.recycle();
        if (!magazine.isEmpty()) {
            this.useMagazine(magazine);
        } else {
            this.magazines.remove(magazine);
        }
    }

    void recycle(Magazine.Slot slot, MagazineHolder holder) {
        this.partialQueue.add(new Handle(slot, holder));
    }

    private void useMagazine(Magazine m) {
        Magazine.Slot s;
        ConcurrentLinkedQueue<Handle> q;
        MagazineHolder h = new MagazineHolder(this, m);
        ArrayList<Handle> handles = new ArrayList<Handle>();
        ConcurrentLinkedQueue<Handle> concurrentLinkedQueue = q = m.isFull() ? this.freeQueue : this.partialQueue;
        while ((s = m.get()) != null) {
            handles.add(new Handle(s, h));
        }
        q.addAll(handles);
    }

    public Closure.Handle newClosureHandle(Closure closure) {
        Handle h = this.partialQueue.poll();
        if (h == null) {
            h = this.freeQueue.poll();
        }
        if (h == null) {
            h = this.allocateNewHandle();
        }
        h.slot.proxy.closure = closure;
        return h;
    }

    private Handle allocateNewHandle() {
        Handle h;
        while ((h = this.partialQueue.poll()) == null && (h = this.freeQueue.poll()) == null) {
            Magazine m = new Magazine(this.callContext);
            this.useMagazine(m);
            this.magazines.add(m);
        }
        return h;
    }

    private static final class Magazine {
        private static final MemoryIO IO = MemoryIO.getInstance();
        private final Foreign foreign = Foreign.getInstance();
        private final CallContext ctx;
        private final long magazine;
        private final Slot[] slots;
        private int next;
        private int freeCount;

        Magazine(CallContext ctx) {
            Proxy proxy;
            long h;
            this.ctx = ctx;
            this.magazine = this.foreign.newClosureMagazine(ctx.getAddress(), Proxy.METHOD, false);
            ArrayList<Slot> slots = new ArrayList<Slot>();
            while ((h = this.foreign.closureMagazineGet(this.magazine, proxy = new Proxy(ctx))) != 0L) {
                Slot s = new Slot(h, proxy);
                slots.add(s);
            }
            this.slots = new Slot[slots.size()];
            slots.toArray(this.slots);
            this.next = 0;
            this.freeCount = this.slots.length;
        }

        Slot get() {
            while (this.freeCount > 0 && this.next < this.slots.length) {
                Slot s = this.slots[this.next++];
                if (!s.autorelease) continue;
                --this.freeCount;
                return s;
            }
            return null;
        }

        boolean isFull() {
            return this.slots.length == this.freeCount;
        }

        boolean isEmpty() {
            return this.freeCount < 1;
        }

        void recycle() {
            for (int i = 0; i < this.slots.length; ++i) {
                Slot s = this.slots[i];
                if (!s.autorelease) continue;
                ++this.freeCount;
                s.proxy.closure = NULL_CLOSURE;
            }
            this.next = 0;
        }

        protected void finalize() throws Throwable {
            try {
                boolean release = true;
                for (int i = 0; i < this.slots.length; ++i) {
                    if (this.slots[i].autorelease) continue;
                    release = false;
                    break;
                }
                if (this.magazine != 0L && release) {
                    this.foreign.freeClosureMagazine(this.magazine);
                }
            }
            finally {
                super.finalize();
            }
        }

        static final class Slot {
            final long handle;
            final long codeAddress;
            final Proxy proxy;
            volatile boolean autorelease;

            public Slot(long handle, Proxy proxy) {
                this.handle = handle;
                this.proxy = proxy;
                this.autorelease = true;
                this.codeAddress = IO.getAddress(handle);
            }
        }
    }

    private static final class Handle
    implements Closure.Handle {
        final MagazineHolder holder;
        final Magazine.Slot slot;
        private volatile boolean disposed;

        Handle(Magazine.Slot slot, MagazineHolder holder) {
            this.slot = slot;
            this.holder = holder;
        }

        @Override
        public long getAddress() {
            if (this.disposed) {
                throw new RuntimeException("trying to access disposed closure handle");
            }
            return this.slot.codeAddress;
        }

        @Override
        public void setAutoRelease(boolean autorelease) {
            if (!this.disposed) {
                this.slot.autorelease = autorelease;
            }
        }

        @Override
        @Deprecated
        public void free() {
            this.dispose();
        }

        @Override
        public synchronized void dispose() {
            if (!this.disposed) {
                this.disposed = true;
                this.slot.autorelease = true;
                this.slot.proxy.closure = NULL_CLOSURE;
                this.holder.pool.recycle(this.slot, this.holder);
            }
        }
    }

    private static final class MagazineHolder {
        final ClosurePool pool;
        final Magazine magazine;

        public MagazineHolder(ClosurePool pool, Magazine magazine) {
            this.pool = pool;
            this.magazine = magazine;
        }

        protected void finalize() throws Throwable {
            try {
                this.pool.recycle(this.magazine);
            }
            finally {
                super.finalize();
            }
        }
    }

    static final class Proxy {
        static final Method METHOD = Proxy.getMethod();
        final CallContext callContext;
        volatile Closure closure = ClosurePool.access$000();

        private static Method getMethod() {
            try {
                return Proxy.class.getDeclaredMethod("invoke", Long.TYPE, Long.TYPE);
            }
            catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        }

        Proxy(CallContext callContext) {
            this.callContext = callContext;
        }

        public void invoke(long retvalAddress, long paramAddress) {
            this.closure.invoke(new DirectClosureBuffer(this.callContext, retvalAddress, paramAddress));
        }
    }
}

