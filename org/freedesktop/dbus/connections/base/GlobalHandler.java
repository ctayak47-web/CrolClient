
package org.freedesktop.dbus.connections.base;

import org.freedesktop.dbus.connections.base.AbstractConnectionBase;
import org.freedesktop.dbus.errors.UnknownObject;
import org.freedesktop.dbus.interfaces.Introspectable;
import org.freedesktop.dbus.interfaces.Peer;
import org.freedesktop.dbus.messages.ExportedObject;

public class GlobalHandler
implements Peer,
Introspectable {
    private final AbstractConnectionBase connection;
    private final String objectpath;

    GlobalHandler(AbstractConnectionBase _abstractConnection) {
        this.connection = _abstractConnection;
        this.objectpath = null;
    }

    GlobalHandler(AbstractConnectionBase _abstractConnection, String _objectpath) {
        this.connection = _abstractConnection;
        this.objectpath = _objectpath;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public void Ping() {
    }

    @Override
    public String Introspect() {
        ExportedObject eo;
        String intro = this.connection.getObjectTree().Introspect(this.objectpath);
        if (null == intro && null != (eo = this.connection.getFallbackContainer().get(this.objectpath))) {
            intro = eo.getIntrospectiondata();
        }
        if (null == intro) {
            throw new UnknownObject("Introspecting on non-existant object");
        }
        return "<!DOCTYPE node PUBLIC \"-
    }

    @Override
    public String getObjectPath() {
        return this.objectpath;
    }

    @Override
    public String GetMachineId() {
        return this.connection.getMachineId();
    }
}

