
package org.freedesktop.dbus.connections.impl;

import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfig;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.connections.impl.BaseConnectionBuilder;
import org.freedesktop.dbus.connections.impl.DirectConnection;
import org.freedesktop.dbus.exceptions.DBusException;

public final class DirectConnectionBuilder
extends BaseConnectionBuilder<DirectConnectionBuilder, DirectConnection> {
    private DirectConnectionBuilder(BusAddress _address) {
        super(DirectConnectionBuilder.class, _address);
    }

    public static DirectConnectionBuilder forAddress(String _address) {
        return new DirectConnectionBuilder(BusAddress.of(_address));
    }

    @Override
    public DirectConnection build() throws DBusException {
        ReceivingServiceConfig rsCfg = this.buildThreadConfig();
        TransportConfig transportCfg = this.buildTransportConfig();
        DirectConnection c = new DirectConnection(transportCfg, rsCfg);
        c.setDisconnectCallback(this.getDisconnectCallback());
        c.setWeakReferences(this.isWeakReference());
        return c;
    }
}

