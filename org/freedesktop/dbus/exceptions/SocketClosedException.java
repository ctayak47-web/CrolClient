
package org.freedesktop.dbus.exceptions;

import java.io.IOException;

public class SocketClosedException
extends IOException {
    private static final long serialVersionUID = 1L;

    public SocketClosedException() {
    }

    public SocketClosedException(String _message, Throwable _cause) {
        super(_message, _cause);
    }

    public SocketClosedException(String _message) {
        super(_message);
    }

    public SocketClosedException(Throwable _cause) {
        super(_cause);
    }
}

