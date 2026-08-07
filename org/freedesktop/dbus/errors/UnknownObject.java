
package org.freedesktop.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class UnknownObject
extends DBusExecutionException {
    private static final long serialVersionUID = 4951706443147828582L;

    public UnknownObject(String _message) {
        super(_message);
    }
}

