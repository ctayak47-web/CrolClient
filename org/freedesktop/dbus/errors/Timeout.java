
package org.freedesktop.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class Timeout
extends DBusExecutionException {
    private static final long serialVersionUID = -1212844876312953745L;

    public Timeout(String _message) {
        super(_message);
    }
}

