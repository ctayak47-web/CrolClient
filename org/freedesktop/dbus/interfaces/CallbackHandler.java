
package org.freedesktop.dbus.interfaces;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public interface CallbackHandler<T> {
    public void handle(T var1);

    public void handleError(DBusExecutionException var1);
}

