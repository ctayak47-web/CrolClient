
package org.freedesktop.dbus.interfaces;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.types.UInt32;

@DBusInterfaceName(value="org.freedesktop.DBus.Monitoring.BecomeMonitor")
public interface Monitoring {
    public void BecomeMonitor(String[] var1, UInt32 var2);
}

