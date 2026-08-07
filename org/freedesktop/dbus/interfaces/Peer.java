
package org.freedesktop.dbus.interfaces;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName(value="org.freedesktop.DBus.Peer")
public interface Peer
extends DBusInterface {
    public void Ping();

    public String GetMachineId();
}

