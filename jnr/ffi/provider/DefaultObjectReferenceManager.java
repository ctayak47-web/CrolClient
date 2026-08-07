
package jnr.ffi.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jnr.ffi.ObjectReferenceManager;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.provider.InAccessibleMemoryIO;

public final class DefaultObjectReferenceManager
extends ObjectReferenceManager {
    private final Runtime runtime;
    private final ConcurrentMap<Long, ObjectReference> references = new ConcurrentHashMap<Long, ObjectReference>();

    public DefaultObjectReferenceManager(Runtime runtime) {
        this.runtime = runtime;
    }

    public Pointer add(Object obj) {
        ObjectReference ptr;
        if (obj == null) {
            throw new IllegalArgumentException("reference to null value not allowed");
        }
        long nextId = this.id(obj);
        while (true) {
            ptr = new ObjectReference(this.runtime, nextId, obj);
            if (this.references.putIfAbsent(nextId, ptr) == null) break;
            ++nextId;
        }
        return ptr;
    }

    @Override
    public boolean remove(Pointer reference) {
        ObjectReference entry = (ObjectReference)this.references.remove(reference.address());
        return entry != null;
    }

    public Object get(Pointer reference) {
        ObjectReference ptr = (ObjectReference)this.references.get(reference.address());
        return ptr != null ? ptr.referent : null;
    }

    private long id(Object obj) {
        return (0xCAFEBABE00000000L | (long)System.identityHashCode(obj) & 0xFFFFFFFFL) & this.runtime.addressMask();
    }

    private static final class ObjectReference
    extends InAccessibleMemoryIO {
        private final Object referent;

        public ObjectReference(Runtime runtime, long address, Object referent) {
            super(runtime, address, true);
            this.referent = referent;
        }

        @Override
        public long size() {
            return 0L;
        }

        public int hashCode() {
            return (int)this.address();
        }

        public boolean equals(Object obj) {
            return obj instanceof Pointer && ((Pointer)obj).address() == this.address();
        }
    }
}

