
package org.freedesktop.dbus.utils;

@FunctionalInterface
public interface IThrowingSupplier<V, T extends Throwable> {
    public V get() throws T;
}

