
package org.freedesktop.dbus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import org.freedesktop.dbus.DBusAsyncReply;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.RemoteObject;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.DBusBoundProperty;
import org.freedesktop.dbus.annotations.MethodNoReply;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.connections.base.AbstractConnectionBase;
import org.freedesktop.dbus.errors.NoReply;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.exceptions.NotConnected;
import org.freedesktop.dbus.interfaces.CallbackHandler;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.Error;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MethodCall;
import org.freedesktop.dbus.propertyref.PropRefRemoteHandler;
import org.freedesktop.dbus.utils.DBusNamingUtil;
import org.freedesktop.dbus.utils.LoggingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteInvocationHandler
implements InvocationHandler {
    public static final int CALL_TYPE_SYNC = 0;
    public static final int CALL_TYPE_ASYNC = 1;
    public static final int CALL_TYPE_CALLBACK = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteInvocationHandler.class);
    AbstractConnection conn;
    RemoteObject remote;

    public RemoteInvocationHandler(AbstractConnection _conn, RemoteObject _remote) {
        this.remote = _remote;
        this.conn = _conn;
    }

    public RemoteObject getRemote() {
        return this.remote;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object invoke(Object _proxy, Method _method, Object[] _args) throws Throwable {
        block32: {
            if (_method.getName().equals("isRemote")) {
                return true;
            }
            if (_method.getName().equals("getObjectPath")) {
                return this.remote.getObjectPath();
            }
            if (_method.getName().equals("clone")) {
                return null;
            }
            if (_method.getName().equals("equals")) {
                try {
                    if (1 == _args.length) {
                        return _args[0] != null && this.remote.equals(((RemoteInvocationHandler)Proxy.getInvocationHandler((Object)_args[0])).remote);
                    }
                    break block32;
                }
                catch (IllegalArgumentException _exIa) {
                    return Boolean.FALSE;
                }
            }
            if (_method.getName().equals("finalize")) {
                return null;
            }
            if (_method.getName().equals("getClass")) {
                return DBusInterface.class;
            }
            if (_method.getName().equals("hashCode")) {
                return this.remote.hashCode();
            }
            if (_method.getName().equals("notify")) {
                RemoteObject remoteObject = this.remote;
                synchronized (remoteObject) {
                    this.remote.notify();
                }
                return null;
            }
            if (_method.getName().equals("notifyAll")) {
                RemoteObject remoteObject = this.remote;
                synchronized (remoteObject) {
                    this.remote.notifyAll();
                }
                return null;
            }
            if (_method.getName().equals("wait")) {
                RemoteObject remoteObject = this.remote;
                synchronized (remoteObject) {
                    Object object;
                    if (_args.length == 0) {
                        this.remote.wait();
                    } else if (_args.length == 1 && (object = _args[0]) instanceof Long) {
                        Long l = (Long)object;
                        this.remote.wait(l);
                    } else if (_args.length == 2 && (object = _args[0]) instanceof Long) {
                        Long l = (Long)object;
                        object = _args[1];
                        if (object instanceof Integer) {
                            Integer i = (Integer)object;
                            this.remote.wait(l, i);
                        }
                    }
                    if (_args.length <= 2) {
                        return null;
                    }
                }
            } else {
                if (_method.getName().equals("toString")) {
                    return this.remote.toString();
                }
                if (_method.isAnnotationPresent(DBusBoundProperty.class)) {
                    return PropRefRemoteHandler.handleDBusBoundProperty(this.conn, this.remote, _method, _args);
                }
            }
        }
        return RemoteInvocationHandler.executeRemoteMethod(this.remote, _method, this.conn, 0, null, _args);
    }

    public static Object convertRV(Object[] _rp, Method _m, AbstractConnection _conn) throws DBusException {
        return RemoteInvocationHandler.convertRV(_rp, new Type[]{_m.getGenericReturnType()}, _m, _conn);
    }

    public static Object convertRV(Object[] _rp, Type[] _types, Method _m, AbstractConnection _conn) throws DBusException {
        Class<?> c = _m.getReturnType();
        Object[] rp = _rp;
        if (rp == null) {
            if (null == c || Void.TYPE.equals(c)) {
                return null;
            }
            throw new DBusException("Wrong return type (got void, expected a value)");
        }
        try {
            LoggingHelper.logIf(LOGGER.isTraceEnabled(), () -> LOGGER.trace("Converting return parameters from {} to type {}", (Object)Arrays.deepToString(_rp), (Object)_m.getGenericReturnType()));
            rp = Marshalling.deSerializeParameters(rp, _types, (AbstractConnectionBase)_conn);
        }
        catch (Exception _ex) {
            LOGGER.debug("Wrong return type.", _ex);
            throw new DBusException(String.format("Wrong return type (failed to de-serialize correct types: %s )", _ex.getMessage()), _ex);
        }
        switch (rp.length) {
            case 0: {
                if (null == c || Void.TYPE.equals(c)) {
                    return null;
                }
                throw new DBusException("Wrong return type (got void, expected a value)");
            }
            case 1: {
                return rp[0];
            }
        }
        if (!Tuple.class.isAssignableFrom(c)) {
            throw new DBusException("Wrong return type (not expecting Tuple)");
        }
        Constructor<?> cons = c.getConstructors()[0];
        try {
            return cons.newInstance(rp);
        }
        catch (Exception _ex) {
            LOGGER.debug("Error creating tuple instance using reflection", _ex);
            throw new DBusException(_ex.getMessage());
        }
    }

    public static Object executeRemoteMethod(RemoteObject _ro, Method _m, AbstractConnection _conn, int _syncmethod, CallbackHandler<?> _callback, Object ... _args) throws DBusException {
        return RemoteInvocationHandler.executeRemoteMethod(_ro, _m, new Type[]{_m.getGenericReturnType()}, _conn, _syncmethod, _callback, _args);
    }

    public static Object executeRemoteMethod(RemoteObject _ro, Method _m, String[] _customSignatures, Type[] _types, AbstractConnection _conn, int _syncmethod, CallbackHandler<?> _callback, Object ... _args) throws DBusException {
        MethodCall call;
        Type[] ts = _m.getGenericParameterTypes();
        String sig = null;
        Object[] args = _args;
        if (ts.length > 0) {
            try {
                sig = Marshalling.getDBusType(ts);
                args = Marshalling.convertParameters(args, ts, _customSignatures, _conn);
            }
            catch (DBusException _ex) {
                throw new DBusExecutionException("Failed to construct D-Bus type: " + _ex.getMessage());
            }
        }
        byte flags = 0;
        if (!_ro.isAutostart()) {
            flags = (byte)(flags | 2);
        }
        if (_syncmethod == 1) {
            flags = (byte)(flags | 0x40);
        }
        if (_m.isAnnotationPresent(MethodNoReply.class)) {
            flags = (byte)(flags | 1);
        }
        try {
            String name = DBusNamingUtil.getMethodName(_m);
            if (null == _ro.getInterface()) {
                call = _conn.getMessageFactory().createMethodCall(null, _ro.getBusName(), _ro.getObjectPath(), null, name, flags, sig, args);
            } else {
                String iface = DBusNamingUtil.getInterfaceName(_ro.getInterface());
                call = _conn.getMessageFactory().createMethodCall(null, _ro.getBusName(), _ro.getObjectPath(), iface, name, flags, sig, args);
            }
        }
        catch (DBusException _ex) {
            LOGGER.debug("Failed to construct outgoing method call.", _ex);
            throw new DBusExecutionException("Failed to construct outgoing method call: " + _ex.getMessage());
        }
        if (!_conn.isConnected()) {
            throw new NotConnected("Not Connected");
        }
        switch (_syncmethod) {
            case 1: {
                _conn.sendMessage(call);
                return new DBusAsyncReply(call, _m, _conn);
            }
            case 2: {
                _conn.queueCallback(call, _m, _callback);
                _conn.sendMessage(call);
                return null;
            }
            case 0: {
                _conn.sendMessage(call);
            }
        }
        if (_m.isAnnotationPresent(MethodNoReply.class)) {
            return null;
        }
        Message reply = call.getReply();
        if (null == reply) {
            throw new NoReply("No reply within specified time");
        }
        if (reply instanceof Error) {
            Error err = (Error)reply;
            err.throwException();
        }
        try {
            return RemoteInvocationHandler.convertRV(reply.getParameters(), _types, _m, _conn);
        }
        catch (DBusException _ex) {
            LOGGER.debug("", _ex);
            throw new DBusExecutionException(_ex.getMessage(), _ex);
        }
    }

    public static Object executeRemoteMethod(RemoteObject _ro, Method _m, Type[] _types, AbstractConnection _conn, int _syncmethod, CallbackHandler<?> _callback, Object ... _args) throws DBusException {
        return RemoteInvocationHandler.executeRemoteMethod(_ro, _m, null, _types, _conn, _syncmethod, _callback, _args);
    }
}

