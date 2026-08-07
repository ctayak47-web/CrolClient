
package org.freedesktop.dbus.connections.transports;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public interface IFileBasedBusAddress {
    public void updatePermissions(String var1, String var2, Set<PosixFilePermission> var3);
}

