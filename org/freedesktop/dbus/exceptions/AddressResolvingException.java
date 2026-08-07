
package org.freedesktop.dbus.exceptions;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public class AddressResolvingException
extends DBusExecutionException {
    private static final long serialVersionUID = -1636993356304776163L;

    public AddressResolvingException(String _message) {
        super(_message);
    }
}

