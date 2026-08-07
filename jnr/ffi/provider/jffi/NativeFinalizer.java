
package jnr.ffi.provider.jffi;

import jnr.ffi.util.ref.FinalizableReferenceQueue;

class NativeFinalizer {
    private final FinalizableReferenceQueue finalizerQueue = new FinalizableReferenceQueue();

    NativeFinalizer() {
    }

    public static NativeFinalizer getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public FinalizableReferenceQueue getFinalizerQueue() {
        return this.finalizerQueue;
    }

    private static final class SingletonHolder {
        private static final NativeFinalizer INSTANCE = new NativeFinalizer();

        private SingletonHolder() {
        }
    }
}

