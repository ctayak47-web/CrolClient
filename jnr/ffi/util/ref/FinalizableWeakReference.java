
package jnr.ffi.util.ref;

import java.lang.ref.WeakReference;
import jnr.ffi.util.ref.FinalizableReference;
import jnr.ffi.util.ref.FinalizableReferenceQueue;

public abstract class FinalizableWeakReference<T>
extends WeakReference<T>
implements FinalizableReference {
    protected FinalizableWeakReference(T referent, FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}

