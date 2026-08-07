
package org.freedesktop.dbus.handlers;

import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.messages.DBusSignal;

public abstract class AbstractSignalHandlerBase<T extends DBusSignal>
implements DBusSigHandler<T> {
    public abstract Class<T> getImplementationClass();
}

