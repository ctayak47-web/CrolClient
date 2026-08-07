
package org.freedesktop.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class ServiceUnknown
extends DBusExecutionException {
    private static final long serialVersionUID = -8634413313381034023L;

    public ServiceUnknown(String _message) {
        super(_message);
    }
}

