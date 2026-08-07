
package jnr.ffi;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

public abstract class ObjectReferenceManager<T> {
    public static <T> ObjectReferenceManager<T> newInstance(Runtime runtime) {
        return runtime.newObjectReferenceManager();
    }

    @Deprecated
    public Pointer newReference(T object) {
        return this.add(object);
    }

    @Deprecated
    public void freeReference(Pointer reference) {
        this.remove(reference);
    }

    @Deprecated
    public T getObject(Pointer reference) {
        return this.get(reference);
    }

    public abstract Pointer add(T var1);

    public abstract boolean remove(Pointer var1);

    public abstract T get(Pointer var1);
}

