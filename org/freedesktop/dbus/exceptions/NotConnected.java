
package org.freedesktop.dbus.exceptions;

import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.FatalException;

public class NotConnected
extends DBusExecutionException
implements FatalException {
    private static final long serialVersionUID = -3566138179099398537L;

    public NotConnected(String _message) {
        super(_message);
    }
}

