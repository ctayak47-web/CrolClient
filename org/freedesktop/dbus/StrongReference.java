
package org.freedesktop.dbus;

import java.lang.ref.WeakReference;

public class StrongReference<T>
extends WeakReference<T> {
    private T referant;

    public StrongReference(T _referant) {
        super(_referant);
        this.referant = _referant;
    }

    @Override
    public void clear() {
        this.referant = null;
    }

    @Override
    public boolean enqueue() {
        return false;
    }

    @Override
    public T get() {
        return this.referant;
    }
}

