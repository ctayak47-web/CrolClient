
package org.freedesktop.dbus;

import org.freedesktop.dbus.messages.Message;

public class DBusCallInfo {
    public static final int NO_REPLY = 1;
    public static final int ASYNC = 256;
    private final String source;
    private final String destination;
    private final String objectpath;
    private final String iface;
    private final String method;
    private final int flags;

    public DBusCallInfo(Message _m) {
        this.source = _m.getSource();
        this.destination = _m.getDestination();
        this.objectpath = _m.getPath();
        this.iface = _m.getInterface();
        this.method = _m.getName();
        this.flags = _m.getFlags();
    }

    public String getSource() {
        return this.source;
    }

    public String getDestination() {
        return this.destination;
    }

    public String getObjectPath() {
        return this.objectpath;
    }

    public String getInterface() {
        return this.iface;
    }

    public String getMethod() {
        return this.method;
    }

    public int getFlags() {
        return this.flags;
    }
}

