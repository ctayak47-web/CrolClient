
package org.freedesktop.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class InvalidMethodArgument
extends DBusExecutionException {
    private static final long serialVersionUID = 2504012938615867394L;

    public InvalidMethodArgument(String _message) {
        super(_message);
    }
}

