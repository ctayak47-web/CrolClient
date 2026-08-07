
package org.freedesktop.dbus.exceptions;

public class DBusExecutionException
extends RuntimeException {
    private static final long serialVersionUID = 6327661667731344250L;
    private String type;

    public DBusExecutionException(String _message) {
        super(_message);
    }

    public DBusExecutionException(String _message, Throwable _cause) {
        super(_message, _cause);
    }

    public void setType(String _type) {
        this.type = _type;
    }

    public String getType() {
        if (null == this.type) {
            return this.getClass().getName();
        }
        return this.type;
    }
}

