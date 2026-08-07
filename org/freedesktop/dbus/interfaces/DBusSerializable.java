
package org.freedesktop.dbus.interfaces;

import org.freedesktop.dbus.exceptions.DBusException;

public interface DBusSerializable {
    public Object[] serialize() throws DBusException;
}

