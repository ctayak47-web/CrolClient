
package org.freedesktop.dbus.interfaces;

import org.freedesktop.dbus.messages.DBusSignal;

public interface DBusSigHandler<T extends DBusSignal> {
    public void handle(T var1);
}

