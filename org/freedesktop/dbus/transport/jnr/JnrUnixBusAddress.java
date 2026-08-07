
package org.freedesktop.dbus.transport.jnr;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.transports.IFileBasedBusAddress;
import org.freedesktop.dbus.utils.Util;

public class JnrUnixBusAddress
extends BusAddress
implements IFileBasedBusAddress {
    public JnrUnixBusAddress(BusAddress _obj) {
        super(_obj);
    }

    public boolean hasPath() {
        return this.hasParameter("path");
    }

    public String getAbstract() {
        return this.getParameterValue("abstract");
    }

    public boolean isAbstract() {
        return this.hasParameter("abstract");
    }

    public String getPath() {
        return this.getParameterValue("path");
    }

    @Override
    public void updatePermissions(String _fileOwner, String _fileGroup, Set<PosixFilePermission> _fileUnixPermissions) {
        Util.setFilePermissions(Path.of(this.getPath(), new String[0]), _fileOwner, _fileGroup, _fileUnixPermissions);
    }
}

