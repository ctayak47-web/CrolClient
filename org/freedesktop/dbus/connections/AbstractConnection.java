
package org.freedesktop.dbus.connections;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import org.freedesktop.dbus.DBusAsyncReply;
import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.RemoteInvocationHandler;
import org.freedesktop.dbus.RemoteObject;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.base.ConnectionMessageHandler;
import org.freedesktop.dbus.connections.base.IncomingMessageThread;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfig;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.exceptions.InvalidSignalException;
import org.freedesktop.dbus.interfaces.CallbackHandler;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.messages.ExportedObject;
import org.freedesktop.dbus.messages.MethodCall;
import org.freedesktop.dbus.messages.ObjectTree;
import org.freedesktop.dbus.utils.DBusObjects;

public abstract class AbstractConnection
extends ConnectionMessageHandler {
    public static final boolean FLOAT_SUPPORT = null != System.getenv("DBUS_JAVA_FLOATS");
    public static final Pattern DOLLAR_PATTERN = Pattern.compile("[$]");
    public static final int MAX_ARRAY_LENGTH = 0x4000000;
    public static final int MAX_NAME_LENGTH = 255;
    private boolean weakreferences = false;

    protected AbstractConnection(TransportConfig _transportConfig, ReceivingServiceConfig _rsCfg) throws DBusException {
        super(_transportConfig, _rsCfg);
    }

    @Override
    protected IncomingMessageThread createReaderThread(BusAddress _busAddress) {
        return new IncomingMessageThread(this, _busAddress);
    }

    protected abstract <T extends DBusSignal> void removeSigHandler(DBusMatchRule var1, DBusSigHandler<T> var2) throws DBusException;

    protected abstract <T extends DBusSignal> AutoCloseable addSigHandler(DBusMatchRule var1, DBusSigHandler<T> var2) throws DBusException;

    protected abstract void removeGenericSigHandler(DBusMatchRule var1, DBusSigHandler<DBusSignal> var2) throws DBusException;

    protected abstract AutoCloseable addGenericSigHandler(DBusMatchRule var1, DBusSigHandler<DBusSignal> var2) throws DBusException;

    protected <T extends DBusInterface> List<Class<?>> findMatchingTypes(Class<T> _type, List<String> _ifaces) {
        ArrayList ifcs = new ArrayList();
        if (_type == null) {
            block2: for (String iface : _ifaces) {
                this.getLogger().debug("Trying interface {}", (Object)iface);
                int j = 0;
                while (j >= 0) {
                    try {
                        Class<?> ifclass = Class.forName(iface);
                        if (ifcs.contains(ifclass)) continue block2;
                        ifcs.add(ifclass);
                        continue block2;
                    }
                    catch (Exception _ex) {
                        this.getLogger().trace("No class found for {}", (Object)iface, (Object)_ex);
                        j = iface.lastIndexOf(46);
                        char[] cs = iface.toCharArray();
                        if (j < 0) continue;
                        cs[j] = 36;
                        iface = String.valueOf(cs);
                    }
                }
            }
        } else {
            ifcs.add(_type);
        }
        return ifcs;
    }

    public void setWeakReferences(boolean _weakreferences) {
        this.weakreferences = _weakreferences;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void exportObject(String _objectPath, DBusInterface _object) throws DBusException {
        if (null == _objectPath || _objectPath.isEmpty()) {
            throw new DBusException("Must Specify an Object Path");
        }
        DBusObjects.requireObjectPath(_objectPath);
        Map<String, ExportedObject> map = this.getExportedObjects();
        synchronized (map) {
            if (null != this.getExportedObjects().get(_objectPath)) {
                throw new DBusException("Object already exported");
            }
            ExportedObject eo = new ExportedObject(_object, this.weakreferences);
            this.getExportedObjects().put(_objectPath, eo);
            ObjectTree objectTree = this.getObjectTree();
            synchronized (objectTree) {
                this.getObjectTree().add(_objectPath, eo, eo.getIntrospectiondata());
            }
        }
    }

    public void exportObject(DBusInterface _object) throws DBusException {
        Objects.requireNonNull(_object, "object must not be null");
        this.exportObject(_object.getObjectPath(), _object);
    }

    public void addFallback(String _objectPrefix, DBusInterface _object) throws DBusException {
        DBusObjects.requireObjectPath(_objectPrefix);
        ExportedObject eo = new ExportedObject(_object, this.weakreferences);
        this.getFallbackContainer().add(_objectPrefix, eo);
    }

    public void removeFallback(String _objectprefix) {
        this.getFallbackContainer().remove(_objectprefix);
    }

    public <T extends DBusSignal> void removeSigHandler(Class<T> _type, DBusSigHandler<T> _handler) throws DBusException {
        this.assertSignal(_type);
        this.removeSigHandler(new DBusMatchRule(_type), _handler);
    }

    public <T extends DBusSignal> void removeSigHandler(Class<T> _type, DBusInterface _object, DBusSigHandler<T> _handler) throws DBusException {
        this.assertSignal(_type);
        String objectPath = this.getImportedObjects().get(_object).getObjectPath();
        DBusObjects.requireObjectPath(objectPath);
        this.removeSigHandler(new DBusMatchRule(_type, null, objectPath), _handler);
    }

    public <T extends DBusSignal> AutoCloseable addSigHandler(Class<T> _type, DBusSigHandler<T> _handler) throws DBusException {
        this.assertSignal(_type);
        return this.addSigHandler(new DBusMatchRule(_type), _handler);
    }

    public <T extends DBusSignal> AutoCloseable addSigHandler(Class<T> _type, DBusInterface _object, DBusSigHandler<T> _handler) throws DBusException {
        this.assertSignal(_type);
        RemoteObject rObj = this.getImportedObjects().get(_object);
        if (rObj == null) {
            throw new DBusException("Not an object exported or imported by this connection");
        }
        String objectPath = rObj.getObjectPath();
        DBusObjects.requireObjectPath(objectPath);
        return this.addSigHandler(new DBusMatchRule(_type, null, objectPath), _handler);
    }

    private <T extends DBusSignal> void assertSignal(Class<T> _type) throws InvalidSignalException {
        if (!DBusSignal.class.isAssignableFrom(_type)) {
            throw new InvalidSignalException(_type);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected <T extends DBusSignal> void addSigHandlerWithoutMatch(Class<? extends DBusSignal> _signal, DBusSigHandler<T> _handler) throws DBusException {
        DBusMatchRule rule = new DBusMatchRule(_signal);
        Map<DBusMatchRule, Queue<DBusSigHandler<? extends DBusSignal>>> map = this.getHandledSignals();
        synchronized (map) {
            Queue<DBusSigHandler<? extends DBusSignal>> v = this.getHandledSignals().get(rule);
            if (null == v) {
                v = new ConcurrentLinkedQueue<DBusSigHandler<? extends DBusSignal>>();
                v.add(_handler);
                this.getHandledSignals().put(rule, v);
            } else {
                v.add(_handler);
            }
        }
    }

    public <A> void callWithCallback(DBusInterface _object, String _m, CallbackHandler<A> _callback, Object ... _parameters) {
        this.getLogger().trace("callWithCallback({}, {}, {})", _object, _m, _callback);
        Class<?>[] types = AbstractConnection.createTypesArray(_parameters);
        RemoteObject ro = this.getImportedObjects().get(_object);
        try {
            Method me = null == ro.getInterface() ? _object.getClass().getMethod(_m, types) : ro.getInterface().getMethod(_m, types);
            RemoteInvocationHandler.executeRemoteMethod(ro, me, this, 2, _callback, _parameters);
        }
        catch (DBusExecutionException _ex) {
            this.getLogger().debug("Error calling callback", _ex);
            throw _ex;
        }
        catch (Exception _ex) {
            this.getLogger().debug("Failed to call callback", _ex);
            throw new DBusExecutionException(_ex.getMessage());
        }
    }

    public DBusAsyncReply<?> callMethodAsync(DBusInterface _object, String _method, Object ... _parameters) {
        Class<?>[] types = AbstractConnection.createTypesArray(_parameters);
        RemoteObject ro = this.getImportedObjects().get(_object);
        try {
            Method me = null == ro.getInterface() ? _object.getClass().getMethod(_method, types) : ro.getInterface().getMethod(_method, types);
            return (DBusAsyncReply)RemoteInvocationHandler.executeRemoteMethod(ro, me, this, 1, null, _parameters);
        }
        catch (DBusExecutionException _ex) {
            this.getLogger().debug("Error calling async method", _ex);
            throw _ex;
        }
        catch (Exception _ex) {
            this.getLogger().debug("Failed to execute async method", _ex);
            throw new DBusExecutionException(_ex.getMessage());
        }
    }

    private static Class<?>[] createTypesArray(Object ... _parameters) {
        if (_parameters == null) {
            return null;
        }
        return (Class[])Arrays.stream(_parameters).filter(p -> p != null).map(p -> {
            if (List.class.isAssignableFrom(p.getClass())) {
                return List.class;
            }
            if (Map.class.isAssignableFrom(p.getClass())) {
                return Map.class;
            }
            if (Set.class.isAssignableFrom(p.getClass())) {
                return Set.class;
            }
            return p.getClass();
        }).toArray(Class[]::new);
    }

    public void queueCallback(MethodCall _call, Method _method, CallbackHandler<?> _callback) {
        this.getCallbackManager().queueCallback(_call, _method, _callback, this);
    }

    public boolean isFileDescriptorSupported() {
        return this.getTransport().isFileDescriptorSupported();
    }
}

