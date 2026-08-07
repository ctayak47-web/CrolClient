
package org.freedesktop.dbus.errors;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class PropertyReadOnly
extends DBusExecutionException {
    private static final long serialVersionUID = -8493757965292570003L;

    public PropertyReadOnly(String _message) {
        super(_message);
    }
}

