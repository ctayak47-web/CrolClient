
package org.freedesktop.dbus.connections.impl;

import java.nio.ByteOrder;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.IDisconnectCallback;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfig;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfigBuilder;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.connections.config.TransportConfigBuilder;
import org.freedesktop.dbus.exceptions.DBusException;

public abstract class BaseConnectionBuilder<R extends BaseConnectionBuilder<R, C>, C extends AbstractConnection> {
    private final Class<R> returnType;
    private boolean weakReference = false;
    private IDisconnectCallback disconnectCallback;
    private final ReceivingServiceConfigBuilder<R> rsConfigBuilder;
    private final TransportConfigBuilder<?, R> transportConfigBuilder;

    protected BaseConnectionBuilder(Class<R> _returnType, BusAddress _address) {
        this.returnType = _returnType;
        this.rsConfigBuilder = new ReceivingServiceConfigBuilder<BaseConnectionBuilder>(this::self);
        this.transportConfigBuilder = new TransportConfigBuilder(this::self);
        this.transportConfigBuilder.withBusAddress(_address);
    }

    R self() {
        return (R)((BaseConnectionBuilder)this.returnType.cast(this));
    }

    protected ReceivingServiceConfig buildThreadConfig() {
        return this.rsConfigBuilder.build();
    }

    protected TransportConfig buildTransportConfig() {
        return this.transportConfigBuilder.build();
    }

    protected boolean isWeakReference() {
        return this.weakReference;
    }

    protected IDisconnectCallback getDisconnectCallback() {
        return this.disconnectCallback;
    }

    public ReceivingServiceConfigBuilder<R> receivingThreadConfig() {
        return this.rsConfigBuilder;
    }

    public TransportConfigBuilder<?, R> transportConfig() {
        return this.transportConfigBuilder;
    }

    public R withWeakReferences(boolean _weakRef) {
        this.weakReference = _weakRef;
        return this.self();
    }

    public R withDisconnectCallback(IDisconnectCallback _disconnectCallback) {
        this.disconnectCallback = _disconnectCallback;
        return this.self();
    }

    public abstract C build() throws DBusException;

    public static byte getSystemEndianness() {
        return ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN) ? (byte)66 : 108;
    }
}

