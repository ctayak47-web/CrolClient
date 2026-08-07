
package org.freedesktop.dbus.exceptions;

public class InvalidBusAddressException
extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public InvalidBusAddressException() {
    }

    public InvalidBusAddressException(String _message, Throwable _cause) {
        super(_message, _cause);
    }

    public InvalidBusAddressException(String _s) {
        super(_s);
    }

    public InvalidBusAddressException(Throwable _cause) {
        super(_cause);
    }
}

