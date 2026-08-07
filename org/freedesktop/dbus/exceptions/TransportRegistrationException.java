
package org.freedesktop.dbus.exceptions;

public class TransportRegistrationException
extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TransportRegistrationException(String _message, Throwable _cause) {
        super(_message, _cause);
    }

    public TransportRegistrationException(String _message) {
        super(_message);
    }
}

