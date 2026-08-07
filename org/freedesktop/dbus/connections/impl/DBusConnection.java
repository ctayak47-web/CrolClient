
package org.freedesktop.dbus.connections.impl;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.RemoteInvocationHandler;
import org.freedesktop.dbus.RemoteObject;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.connections.IDisconnectAction;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfig;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.exceptions.InvalidBusNameException;
import org.freedesktop.dbus.exceptions.InvalidObjectPathException;
import org.freedesktop.dbus.exceptions.NotConnected;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.interfaces.Introspectable;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.messages.ExportedObject;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.utils.CommonRegexPattern;
import org.freedesktop.dbus.utils.DBusObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DBusConnection
extends AbstractConnection {
    static final ConcurrentMap<String, DBusConnection> CONNECTIONS = new ConcurrentHashMap<String, DBusConnection>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<String> busnames;
    private final String machineId;
    private DBus dbus;
    private boolean registered;
    final AtomicInteger concurrentConnections = new AtomicInteger(1);
    private final boolean shared;

    DBusConnection(boolean _shared, String _machineId, TransportConfig _tranportCfg, ReceivingServiceConfig _rsCfg) throws DBusException {
        super(_tranportCfg, _rsCfg);
        this.busnames = new ArrayList<String>();
        this.machineId = _machineId;
        this.shared = _shared;
    }

    private AtomicInteger getConcurrentConnections() {
        return this.concurrentConnections;
    }

    void connectImpl() throws DBusException {
        try {
            this.listen();
        }
        catch (IOException _ex) {
            throw new DBusException(_ex);
        }
        SigHandler h = new SigHandler();
        this.addSigHandlerWithoutMatch(DBus.NameAcquired.class, h);
        if (this.getTransportConfig().isRegisterSelf() && this.getTransport().isConnected()) {
            this.register();
        }
    }

    public void register() throws DBusException {
        if (this.registered) {
            return;
        }
        this.dbus = this.getRemoteObject("org.freedesktop.DBus", "/org/freedesktop/DBus", DBus.class);
        try {
            this.busnames.add(this.dbus.Hello());
            this.registered = true;
        }
        catch (DBusExecutionException _ex) {
            this.logger.debug("Error while doing 'Hello' handshake", _ex);
            throw new DBusException(_ex.getMessage(), _ex);
        }
    }

    public <T extends DBusInterface> T dynamicProxy(String _source, String _path, Class<T> _type) throws DBusException {
        this.logger.debug("Introspecting {} on {} for dynamic proxy creation", (Object)_path, (Object)_source);
        try {
            Introspectable intro = this.getRemoteObject(_source, _path, Introspectable.class);
            String data = intro.Introspect();
            this.logger.trace("Got introspection data: {}", (Object)data);
            String[] tags = CommonRegexPattern.PROXY_SPLIT_PATTERN.split(data);
            List<String> ifaces = Arrays.stream(tags).filter(t -> t.startsWith("interface")).map(t -> CommonRegexPattern.IFACE_PATTERN.matcher((CharSequence)t).replaceAll("$1")).map(i -> {
                if (i.startsWith("org.freedesktop.DBus.")) {
                    return CommonRegexPattern.DBUS_IFACE_PATTERN.matcher((CharSequence)i).replaceAll("$1");
                }
                return i;
            }).collect(Collectors.toList());
            List<Class<?>> ifcs = this.findMatchingTypes(_type, ifaces);
            if (ifcs.isEmpty()) {
                ifcs.add(DBusInterface.class);
            }
            RemoteObject ro = new RemoteObject(_source, _path, _type, false);
            DBusInterface newi = (DBusInterface)Proxy.newProxyInstance(ifcs.get(0).getClassLoader(), (Class[])ifcs.toArray(Class[]::new), new RemoteInvocationHandler(this, ro));
            this.getImportedObjects().put(newi, ro);
            return (T)newi;
        }
        catch (Exception _ex) {
            this.logger.debug("Cannot create proxy object", _ex);
            throw new DBusException(String.format("Failed to create proxy object for %s exported by %s. Reason: %s", _path, _source, _ex.getMessage()));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T extends DBusInterface> T getExportedObject(String _source, String _path, Class<T> _type) throws DBusException {
        ExportedObject o;
        Map<String, ExportedObject> map = this.getExportedObjects();
        synchronized (map) {
            o = this.getExportedObjects().get(_path);
        }
        if (null != o && o.getObject().get() == null) {
            this.unExportObject(_path);
            o = null;
        }
        if (null != o) {
            return (T)o.getObject().get();
        }
        if (null == _source) {
            throw new DBusException("Not an object exported by this connection and no remote specified");
        }
        return this.dynamicProxy(_source, _path, _type);
    }

    @Override
    public DBusInterface getExportedObject(String _source, String _path) throws DBusException {
        return this.getExportedObject(_source, _path, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void releaseBusName(String _busname) throws DBusException {
        DBusObjects.requireBusName(_busname);
        try {
            this.dbus.ReleaseName(_busname);
        }
        catch (DBusExecutionException _ex) {
            this.logger.debug("Failed to release bus name", _ex);
            throw new DBusException(_ex.getMessage());
        }
        List<String> list = this.busnames;
        synchronized (list) {
            this.busnames.remove(_busname);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void requestBusName(String _busname) throws DBusException {
        UInt32 rv;
        DBusObjects.requireBusName(_busname);
        try {
            rv = this.dbus.RequestName(_busname, new UInt32(6L));
        }
        catch (DBusExecutionException _exDb) {
            this.logger.debug("Failed to request bus name", _exDb);
            throw new DBusException(_exDb);
        }
        if (rv.intValue() == 2 || rv.intValue() == 3) {
            throw new DBusException("Failed to register bus name");
        }
        List<String> list = this.busnames;
        synchronized (list) {
            this.busnames.add(_busname);
        }
    }

    public String getUniqueName() {
        return this.busnames.get(0);
    }

    public String[] getNames() {
        TreeSet<String> names = new TreeSet<String>();
        names.addAll(this.busnames);
        return (String[])names.toArray(String[]::new);
    }

    public <I extends DBusInterface> I getPeerRemoteObject(String _busname, String _objectpath, Class<I> _type) throws DBusException {
        return this.getPeerRemoteObject(_busname, _objectpath, _type, true);
    }

    public DBusInterface getPeerRemoteObject(String _busname, String _objectpath) throws InvalidBusNameException, DBusException {
        DBusObjects.requireBusNameOrConnectionId(_busname);
        String unique = this.dbus.GetNameOwner(_busname);
        return this.dynamicProxy(unique, _objectpath, null);
    }

    public DBusInterface getRemoteObject(String _busname, String _objectpath) throws DBusException, InvalidBusNameException, InvalidObjectPathException {
        DBusObjects.requireBusNameOrConnectionId(_busname);
        DBusObjects.requireObjectPath(_objectpath);
        return this.dynamicProxy(_busname, _objectpath, null);
    }

    public <I extends DBusInterface> I getPeerRemoteObject(String _busname, String _objectpath, Class<I> _type, boolean _autostart) throws DBusException, InvalidBusNameException {
        if (null == _busname) {
            throw new InvalidBusNameException();
        }
        DBusObjects.requireBusNameOrConnectionId(_busname);
        String unique = this.dbus.GetNameOwner(_busname);
        return this.getRemoteObject(unique, _objectpath, _type, _autostart);
    }

    public <I extends DBusInterface> I getRemoteObject(String _busname, String _objectpath, Class<I> _type) throws DBusException {
        return this.getRemoteObject(_busname, _objectpath, _type, true);
    }

    public <I extends DBusInterface> I getRemoteObject(String _busname, String _objectpath, Class<I> _type, boolean _autostart) throws DBusException {
        if (_type == null) {
            throw new ClassCastException("Not A DBus Interface");
        }
        DBusObjects.requireBusNameOrConnectionId(_busname);
        DBusObjects.requireObjectPath(_objectpath);
        if (!DBusInterface.class.isAssignableFrom(_type)) {
            throw new ClassCastException("Not A DBus Interface");
        }
        if (_type.getName().equals(_type.getSimpleName())) {
            throw new DBusException("DBusInterfaces cannot be declared outside a package");
        }
        RemoteObject ro = new RemoteObject(_busname, _objectpath, _type, _autostart);
        DBusInterface i = (DBusInterface)Proxy.newProxyInstance(_type.getClassLoader(), new Class[]{_type}, new RemoteInvocationHandler(this, ro));
        this.getImportedObjects().put(i, ro);
        return (I)i;
    }

    public <T extends DBusSignal> void removeSigHandler(Class<T> _type, String _source, DBusSigHandler<T> _handler) throws DBusException {
        this.validateSignal(_type, _source);
        this.removeSigHandler(new DBusMatchRule(_type, _source, null), _handler);
    }

    public <T extends DBusSignal> void removeSigHandler(Class<T> _type, String _source, DBusInterface _object, DBusSigHandler<T> _handler) throws DBusException {
        this.validateSignal(_type, _source);
        String objectPath = this.getImportedObjects().get(_object).getObjectPath();
        DBusObjects.requireObjectPath(objectPath);
        this.removeSigHandler(new DBusMatchRule(_type, _source, objectPath), _handler);
    }

    @Override
    protected <T extends DBusSignal> void removeSigHandler(DBusMatchRule _rule, DBusSigHandler<T> _handler) throws DBusException {
        Queue<DBusSigHandler<? extends DBusSignal>> dbusSignalList = this.getHandledSignals().get(_rule);
        if (null != dbusSignalList) {
            dbusSignalList.remove(_handler);
            if (dbusSignalList.isEmpty()) {
                this.getHandledSignals().remove(_rule);
                try {
                    this.dbus.RemoveMatch(_rule.toString());
                }
                catch (NotConnected _ex) {
                    this.logger.debug("No connection.", _ex);
                }
                catch (DBusExecutionException _ex) {
                    this.logger.debug("Error removing signal", _ex);
                    throw new DBusException(_ex);
                }
            }
        }
    }

    public <T extends DBusSignal> AutoCloseable addSigHandler(final Class<T> _type, final String _source, final DBusSigHandler<T> _handler) throws DBusException {
        this.validateSignal(_type, _source);
        this.addSigHandler(new DBusMatchRule(_type, _source, null), _handler);
        return new AutoCloseable(){

            @Override
            public void close() throws DBusException {
                DBusConnection.this.removeSigHandler(_type, _source, _handler);
            }
        };
    }

    public <T extends DBusSignal> AutoCloseable addSigHandler(final Class<T> _type, final String _source, final DBusInterface _object, final DBusSigHandler<T> _handler) throws DBusException {
        this.validateSignal(_type, _source);
        String objectPath = this.getImportedObjects().get(_object).getObjectPath();
        DBusObjects.requireObjectPath(objectPath);
        this.addSigHandler(new DBusMatchRule(_type, _source, objectPath), _handler);
        return new AutoCloseable(){

            @Override
            public void close() throws DBusException {
                DBusConnection.this.removeSigHandler(_type, _source, _object, _handler);
            }
        };
    }

    private <T extends DBusSignal> void validateSignal(Class<T> _type, String _source) throws DBusException {
        if (!DBusSignal.class.isAssignableFrom(_type)) {
            throw new ClassCastException("Not A DBus Signal");
        }
        DBusObjects.requireNotBusName(_source, "Cannot watch for signals based on well known bus name as source. Only unique names supported");
        DBusObjects.requireConnectionId(_source);
    }

    @Override
    public <T extends DBusSignal> AutoCloseable addSigHandler(final DBusMatchRule _rule, final DBusSigHandler<T> _handler) throws DBusException {
        Objects.requireNonNull(_rule, "Match rule cannot be null");
        Objects.requireNonNull(_handler, "Handler cannot be null");
        AtomicBoolean addMatch = new AtomicBoolean(false);
        Queue dbusSignalList = this.getHandledSignals().computeIfAbsent(_rule, v -> {
            ConcurrentLinkedQueue signalList = new ConcurrentLinkedQueue();
            addMatch.set(true);
            return signalList;
        });
        dbusSignalList.add(_handler);
        if (addMatch.get()) {
            try {
                this.dbus.AddMatch(_rule.toString());
            }
            catch (DBusExecutionException _ex) {
                this.logger.debug("Cannot add match rule: " + _rule.toString(), _ex);
                throw new DBusException("Cannot add match rule.", _ex);
            }
        }
        return new AutoCloseable(){

            @Override
            public void close() throws DBusException {
                DBusConnection.this.removeSigHandler(_rule, _handler);
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void disconnect() {
        if (!this.isConnected()) {
            return;
        }
        if (this.shared) {
            ConcurrentMap<String, DBusConnection> concurrentMap = CONNECTIONS;
            synchronized (concurrentMap) {
                DBusConnection connection = (DBusConnection)CONNECTIONS.get(this.getAddress().toString());
                if (connection != null) {
                    if (connection.getConcurrentConnections().get() <= 1) {
                        CONNECTIONS.remove(this.getAddress().toString());
                        super.disconnect();
                    } else {
                        this.logger.debug("Still {} connections left, decreasing connection counter", (Object)(connection.getConcurrentConnections().get() - 1));
                        Optional.ofNullable(this.getDisconnectCallback()).ifPresent(cb -> cb.requestedDisconnect(connection.getConcurrentConnections().get()));
                        connection.getConcurrentConnections().decrementAndGet();
                    }
                }
            }
        } else {
            IDisconnectAction beforeDisconnectAction = () -> {
                Map<String, ExportedObject> exportedObjects;
                List<String> list = this.busnames;
                synchronized (list) {
                    List<String> lBusNames = this.busnames.stream().filter(DBusObjects::validateBusName).collect(Collectors.toList());
                    lBusNames.forEach(busName -> {
                        try {
                            this.releaseBusName((String)busName);
                        }
                        catch (DBusException _ex) {
                            this.logger.error("Error while releasing busName '" + busName + "'.", _ex);
                        }
                    });
                }
                Map<String, ExportedObject> map = exportedObjects = this.getExportedObjects();
                synchronized (map) {
                    List exportedKeys = exportedObjects.keySet().stream().filter(f -> f != null).collect(Collectors.toList());
                    for (String key : exportedKeys) {
                        this.unExportObject(key);
                    }
                }
            };
            super.disconnect(beforeDisconnectAction, null);
        }
    }

    @Override
    public void close() throws IOException {
        this.disconnect();
    }

    @Override
    public String getMachineId() {
        return this.machineId;
    }

    @Override
    public void removeGenericSigHandler(DBusMatchRule _rule, DBusSigHandler<DBusSignal> _handler) throws DBusException {
        Queue<DBusSigHandler<DBusSignal>> genericSignalsList = this.getGenericHandledSignals().get(_rule);
        if (null != genericSignalsList) {
            genericSignalsList.remove(_handler);
            if (genericSignalsList.isEmpty()) {
                this.getGenericHandledSignals().remove(_rule);
                try {
                    this.dbus.RemoveMatch(_rule.toString());
                }
                catch (NotConnected _ex) {
                    this.logger.debug("No connection.", _ex);
                }
                catch (DBusExecutionException _ex) {
                    this.logger.debug("Error removing generic signal", _ex);
                    throw new DBusException(_ex);
                }
            }
        }
    }

    @Override
    public AutoCloseable addGenericSigHandler(final DBusMatchRule _rule, final DBusSigHandler<DBusSignal> _handler) throws DBusException {
        AtomicBoolean addMatch = new AtomicBoolean(false);
        Queue genericSignalsList = this.getGenericHandledSignals().computeIfAbsent(_rule, v -> {
            ConcurrentLinkedQueue signalsList = new ConcurrentLinkedQueue();
            addMatch.set(true);
            return signalsList;
        });
        genericSignalsList.add(_handler);
        if (addMatch.get()) {
            try {
                this.dbus.AddMatch(_rule.toString());
            }
            catch (DBusExecutionException _ex) {
                this.logger.debug("Error adding signal handler", _ex);
                throw new DBusException(_ex.getMessage());
            }
        }
        return new AutoCloseable(){

            @Override
            public void close() throws DBusException {
                DBusConnection.this.removeGenericSigHandler(_rule, _handler);
            }
        };
    }

    private final class SigHandler
    implements DBusSigHandler<DBusSignal> {
        private SigHandler() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void handle(DBusSignal _signal) {
            if (_signal instanceof DBus.NameAcquired) {
                DBus.NameAcquired na = (DBus.NameAcquired)_signal;
                List<String> list = DBusConnection.this.busnames;
                synchronized (list) {
                    DBusConnection.this.busnames.add(na.name);
                }
            }
        }
    }

    public static enum DBusBusType {
        SYSTEM,
        SESSION;

    }
}

