
package org.freedesktop.dbus.interfaces;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName(value="org.freedesktop.DBus.Introspectable")
public interface Introspectable
extends DBusInterface {
    public String Introspect();
}

