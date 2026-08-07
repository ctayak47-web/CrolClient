
package org.freedesktop.dbus.exceptions;

import org.freedesktop.dbus.exceptions.DBusException;

public class InvalidSignalException
extends DBusException {
    private static final long serialVersionUID = 1L;

    public InvalidSignalException(String _message, Throwable _cause) {
        super(_message, _cause);
    }

    public InvalidSignalException(String _message) {
        super(_message);
    }

    public InvalidSignalException(Class<?> _clz) {
        super((String)(_clz == null ? "Null is not a signal" : _clz.getName() + " is not a signal"));
    }

    public InvalidSignalException(Throwable _cause) {
        super(_cause);
    }
}

