
package org.freedesktop.dbus.interfaces;

public interface DBusInterface {
    default public boolean isRemote() {
        return false;
    }

    public String getObjectPath();
}

