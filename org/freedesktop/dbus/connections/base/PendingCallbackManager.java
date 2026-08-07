
package org.freedesktop.dbus.connections.base;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.freedesktop.dbus.DBusAsyncReply;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.interfaces.CallbackHandler;
import org.freedesktop.dbus.messages.MethodCall;

public class PendingCallbackManager {
    private final Map<MethodCall, CallbackHandler<? extends Object>> pendingCallbacks = new ConcurrentHashMap<MethodCall, CallbackHandler<? extends Object>>();
    private final Map<MethodCall, DBusAsyncReply<?>> pendingCallbackReplys = new ConcurrentHashMap();

    PendingCallbackManager() {
    }

    public synchronized void queueCallback(MethodCall _call, Method _method, CallbackHandler<?> _callback, AbstractConnection _connection) {
        this.pendingCallbacks.put(_call, _callback);
        this.pendingCallbackReplys.put(_call, new DBusAsyncReply(_call, _method, _connection));
    }

    public synchronized CallbackHandler<? extends Object> removeCallback(MethodCall _methodCall) {
        this.pendingCallbackReplys.remove(_methodCall);
        return this.pendingCallbacks.remove(_methodCall);
    }

    public synchronized CallbackHandler<? extends Object> getCallback(MethodCall _methodCall) {
        return this.pendingCallbacks.get(_methodCall);
    }

    public synchronized DBusAsyncReply<?> getCallbackReply(MethodCall _methodCall) {
        return this.pendingCallbackReplys.get(_methodCall);
    }
}

