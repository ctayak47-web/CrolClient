
package org.freedesktop.dbus;

import org.freedesktop.dbus.interfaces.DBusInterface;

public class RemoteObject {
    private final String busname;
    private final String objectpath;
    private final Class<? extends DBusInterface> iface;
    private final boolean autostart;

    public RemoteObject(String _busname, String _objectpath, Class<? extends DBusInterface> _iface, boolean _autostart) {
        this.busname = _busname;
        this.objectpath = _objectpath;
        this.iface = _iface;
        this.autostart = _autostart;
    }

    public boolean equals(Object _o) {
        if (!(_o instanceof RemoteObject)) {
            return false;
        }
        RemoteObject them = (RemoteObject)_o;
        if (!them.objectpath.equals(this.objectpath)) {
            return false;
        }
        if (null == this.busname && null != them.busname) {
            return false;
        }
        if (null != this.busname && null == them.busname) {
            return false;
        }
        if (null != them.busname && !them.busname.equals(this.busname)) {
            return false;
        }
        if (null == this.iface && null != them.iface) {
            return false;
        }
        if (null != this.iface && null == them.iface) {
            return false;
        }
        return null == them.iface || them.iface.equals(this.iface);
    }

    public int hashCode() {
        return (null == this.busname ? 0 : this.busname.hashCode()) + this.objectpath.hashCode() + (null == this.iface ? 0 : this.iface.hashCode());
    }

    public boolean isAutostart() {
        return this.autostart;
    }

    public String getBusName() {
        return this.busname;
    }

    public String getObjectPath() {
        return this.objectpath;
    }

    public Class<? extends DBusInterface> getInterface() {
        return this.iface;
    }

    public String toString() {
        return this.busname + ":" + this.objectpath + ":" + String.valueOf(this.iface);
    }
}

