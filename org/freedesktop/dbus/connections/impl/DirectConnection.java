
package org.freedesktop.dbus.connections.impl;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.RemoteInvocationHandler;
import org.freedesktop.dbus.RemoteObject;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfig;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.interfaces.Introspectable;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.messages.ExportedObject;
import org.freedesktop.dbus.utils.AddressBuilder;
import org.freedesktop.dbus.utils.CommonRegexPattern;
import org.freedesktop.dbus.utils.DBusObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectConnection
extends AbstractConnection {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String machineId = AddressBuilder.createMachineId();

    DirectConnection(TransportConfig _transportCfg, ReceivingServiceConfig _rsCfg) throws DBusException {
        super(_transportCfg, _rsCfg);
        if (!this.getAddress().isServer()) {
            try {
                this.listen();
            }
            catch (IOException _ex) {
                throw new DBusException(_ex);
            }
        }
    }

    @Override
    public void listen() throws IOException {
        if (this.getAddress().isServer()) {
            this.getTransport().listen();
        }
        super.listen();
    }

    <T extends DBusInterface> T dynamicProxy(String _path, Class<T> _type) throws DBusException {
        try {
            Introspectable intro = this.getRemoteObject(_path, Introspectable.class);
            String data = intro.Introspect();
            String[] tags = CommonRegexPattern.PROXY_SPLIT_PATTERN.split(data);
            List<String> ifaces = Arrays.stream(tags).filter(t -> t.startsWith("interface")).map(t -> CommonRegexPattern.IFACE_PATTERN.matcher((CharSequence)t).replaceAll("$1")).collect(Collectors.toList());
            List<Class<?>> ifcs = this.findMatchingTypes(_type, ifaces);
            if (ifcs.isEmpty()) {
                throw new DBusException("Could not find an interface to cast to");
            }
            RemoteObject ro = new RemoteObject(null, _path, _type, false);
            DBusInterface newi = (DBusInterface)Proxy.newProxyInstance(ifcs.get(0).getClassLoader(), ifcs.toArray(new Class[0]), new RemoteInvocationHandler(this, ro));
            this.getImportedObjects().put(newi, ro);
            return (T)newi;
        }
        catch (Exception _ex) {
            this.logger.debug("Error creating dynamic proxy", _ex);
            throw new DBusException(String.format("Failed to create proxy object for %s; reason: %s.", _path, _ex.getMessage()));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    <T extends DBusInterface> T getExportedObject(String _path, Class<T> _type) throws DBusException {
        ExportedObject o = null;
        Map<String, ExportedObject> map = this.getExportedObjects();
        synchronized (map) {
            o = this.getExportedObjects().get(_path);
        }
        if (null != o && null == o.getObject().get()) {
            this.unExportObject(_path);
            o = null;
        }
        if (null != o) {
            return (T)o.getObject().get();
        }
        return this.dynamicProxy(_path, _type);
    }

    public DBusInterface getRemoteObject(String _objectPath) throws DBusException {
        if (null == _objectPath) {
            throw new DBusException("Invalid object path: null");
        }
        DBusObjects.requireObjectPath(_objectPath);
        return this.dynamicProxy(_objectPath, null);
    }

    public <T extends DBusInterface> T getRemoteObject(String _objectPath, Class<T> _type) throws DBusException {
        if (null == _objectPath) {
            throw new DBusException("Invalid object path: null");
        }
        if (null == _type) {
            throw new ClassCastException("Not A DBus Interface");
        }
        DBusObjects.requireObjectPath(_objectPath);
        if (!DBusInterface.class.isAssignableFrom(_type)) {
            throw new ClassCastException("Not A DBus Interface");
        }
        if (_type.getName().equals(_type.getSimpleName())) {
            throw new DBusException("DBusInterfaces cannot be declared outside a package");
        }
        RemoteObject ro = new RemoteObject(null, _objectPath, _type, false);
        DBusInterface i = (DBusInterface)Proxy.newProxyInstance(_type.getClassLoader(), new Class[]{_type}, new RemoteInvocationHandler(this, ro));
        this.getImportedObjects().put(i, ro);
        return (T)i;
    }

    @Override
    protected <T extends DBusSignal> void removeSigHandler(DBusMatchRule _rule, DBusSigHandler<T> _handler) throws DBusException {
        Queue<DBusSigHandler<? extends DBusSignal>> v = this.getHandledSignals().get(_rule);
        if (v != null) {
            v.remove(_handler);
            if (v.isEmpty()) {
                this.getHandledSignals().remove(_rule);
            }
        }
    }

    @Override
    protected <T extends DBusSignal> AutoCloseable addSigHandler(DBusMatchRule _rule, DBusSigHandler<T> _handler) throws DBusException {
        Queue v = this.getHandledSignals().computeIfAbsent(_rule, val -> new ConcurrentLinkedQueue());
        v.add(_handler);
        return () -> this.removeSigHandler(_rule, _handler);
    }

    @Override
    protected void removeGenericSigHandler(DBusMatchRule _rule, DBusSigHandler<DBusSignal> _handler) throws DBusException {
        Queue<DBusSigHandler<DBusSignal>> v = this.getGenericHandledSignals().get(_rule);
        if (v != null) {
            v.remove(_handler);
            if (v.isEmpty()) {
                this.getGenericHandledSignals().remove(_rule);
            }
        }
    }

    @Override
    protected AutoCloseable addGenericSigHandler(DBusMatchRule _rule, DBusSigHandler<DBusSignal> _handler) throws DBusException {
        Queue v = this.getGenericHandledSignals().computeIfAbsent(_rule, val -> new ConcurrentLinkedQueue());
        v.add(_handler);
        return () -> this.removeGenericSigHandler(_rule, _handler);
    }

    @Override
    public <T extends DBusInterface> T getExportedObject(String _source, String _path, Class<T> _type) throws DBusException {
        return this.getExportedObject(_path, _type);
    }

    @Override
    public String getMachineId() {
        return this.machineId;
    }

    @Override
    public DBusInterface getExportedObject(String _source, String _path) throws DBusException {
        return this.getExportedObject(_path, (Class)null);
    }
}

