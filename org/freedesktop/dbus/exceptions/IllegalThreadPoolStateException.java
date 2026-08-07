
package org.freedesktop.dbus.exceptions;

public class IllegalThreadPoolStateException
extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public IllegalThreadPoolStateException() {
    }

    public IllegalThreadPoolStateException(String _message, Throwable _cause) {
        super(_message, _cause);
    }

    public IllegalThreadPoolStateException(String _s) {
        super(_s);
    }

    public IllegalThreadPoolStateException(Throwable _cause) {
        super(_cause);
    }
}

