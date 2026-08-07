
package com.kenai.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Closure;
import com.kenai.jffi.Foreign;
import com.kenai.jffi.MemoryIO;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ClosureMagazine {
    private final Foreign foreign;
    private final CallContext callContext;
    private final long magazineAddress;
    private volatile int disposed;
    private static final AtomicIntegerFieldUpdater<ClosureMagazine> UPDATER = AtomicIntegerFieldUpdater.newUpdater(ClosureMagazine.class, "disposed");

    ClosureMagazine(Foreign foreign, CallContext callContext, long magazineAddress) {
        this.foreign = foreign;
        this.callContext = callContext;
        this.magazineAddress = magazineAddress;
    }

    public Closure.Handle allocate(Object proxy) {
        long closureAddress = this.foreign.closureMagazineGet(this.magazineAddress, proxy);
        return closureAddress != 0L ? new Handle(this, closureAddress, MemoryIO.getInstance().getAddress(closureAddress)) : null;
    }

    public void dispose() {
        int disposed = UPDATER.getAndSet(this, 1);
        if (this.magazineAddress != 0L && disposed == 0) {
            this.foreign.freeClosureMagazine(this.magazineAddress);
        }
    }

    protected void finalize() throws Throwable {
        try {
            int disposed = UPDATER.getAndSet(this, 1);
            if (this.magazineAddress != 0L && disposed == 0) {
                this.foreign.freeClosureMagazine(this.magazineAddress);
            }
        }
        catch (Throwable t) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "exception when freeing " + this.getClass() + ": %s", t.getLocalizedMessage());
        }
        finally {
            super.finalize();
        }
    }

    private static final class Handle
    implements Closure.Handle {
        private final ClosureMagazine magazine;
        private final long closureAddress;
        private final long codeAddress;

        private Handle(ClosureMagazine magazine, long closureAddress, long codeAddress) {
            this.magazine = magazine;
            this.closureAddress = closureAddress;
            this.codeAddress = codeAddress;
        }

        @Override
        public long getAddress() {
            return this.codeAddress;
        }

        @Override
        public void setAutoRelease(boolean autorelease) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public void free() {
        }
    }
}

