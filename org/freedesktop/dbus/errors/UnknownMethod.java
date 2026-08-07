
package org.freedesktop.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class UnknownMethod
extends DBusExecutionException {
    private static final long serialVersionUID = -6712037259368315246L;

    public UnknownMethod(String _message) {
        super(_message);
    }
}

