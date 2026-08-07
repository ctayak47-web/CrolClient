
package jnr.ffi.util.ref;

import java.lang.ref.SoftReference;
import jnr.ffi.util.ref.FinalizableReference;
import jnr.ffi.util.ref.FinalizableReferenceQueue;

public abstract class FinalizableSoftReference<T>
extends SoftReference<T>
implements FinalizableReference {
    protected FinalizableSoftReference(T referent, FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}

