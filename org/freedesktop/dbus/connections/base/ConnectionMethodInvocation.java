
package org.freedesktop.dbus.connections.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import org.freedesktop.dbus.DBusCallInfo;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.connections.base.AbstractConnectionBase;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfig;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.errors.UnknownMethod;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MethodCall;
import org.freedesktop.dbus.messages.MethodReturn;
import org.freedesktop.dbus.utils.LoggingHelper;

/*
 * Uses 'sealed' constructs - enablewith --sealed true
 */
public abstract class ConnectionMethodInvocation
extends AbstractConnectionBase {
    protected ConnectionMethodInvocation(TransportConfig _transportConfig, ReceivingServiceConfig _rsCfg) throws DBusException {
        super(_transportConfig, _rsCfg);
    }

    protected abstract void handleException(Message var1, DBusExecutionException var2);

    protected void queueInvokeMethod(MethodCall _methodCall, Method _meth, Object _ob) {
        this.getLogger().trace("Adding Runnable for method {}", (Object)_meth);
        boolean noReply = 1 == (_methodCall.getFlags() & 1);
        this.getReceivingService().execMethodCallHandler(() -> this.setupAndInvoke(_methodCall, _meth, _ob, noReply));
    }

    protected Object setupAndInvoke(MethodCall _methodCall, Method _meth, Object _ob, boolean _noReply) {
        this.getLogger().debug("Running method {} for remote call", (Object)_meth);
        try {
            Type[] ts = _meth.getGenericParameterTypes();
            Object[] params2 = _methodCall.getParameters();
            _methodCall.setArgs(Marshalling.deSerializeParameters(params2, ts, (AbstractConnectionBase)this));
            LoggingHelper.logIf(this.getLogger().isTraceEnabled(), () -> {
                try {
                    Object[] params3 = _methodCall.getParameters();
                    this.getLogger().trace("Deserialised {} to types {}", (Object)Arrays.deepToString(params3), (Object)Arrays.deepToString(ts));
                }
                catch (Exception _ex) {
                    this.getLogger().trace("Error getting method call parameters", _ex);
                }
            });
        }
        catch (Exception _ex) {
            this.getLogger().debug("", _ex);
            this.handleException(_methodCall, new UnknownMethod("Failure in de-serializing message: " + String.valueOf(_ex)));
            return null;
        }
        return this.invokeMethodAndReply(_methodCall, _meth, _ob, _noReply);
    }

    protected Object invokeMethodAndReply(MethodCall _methodCall, Method _me, Object _ob, boolean _noreply) {
        try {
            Object result = this.invokeMethod(_methodCall, _me, _ob);
            if (!_noreply) {
                this.invokedMethodReply(_methodCall, _me, result);
            }
            return result;
        }
        catch (DBusExecutionException _ex) {
            this.getLogger().debug("Failed to invoke method call", _ex);
            this.handleException(_methodCall, _ex);
        }
        catch (Throwable _ex) {
            this.getLogger().debug("Error invoking method call", _ex);
            this.handleException(_methodCall, new DBusExecutionException(String.format("Error Executing Method %s.%s: %s", _methodCall.getInterface(), _methodCall.getName(), _ex.getMessage())));
        }
        return null;
    }

    protected void invokedMethodReply(MethodCall _methodCall, Method _me, Object _result) throws DBusException {
        MethodReturn reply;
        if (Void.TYPE.equals(_me.getReturnType())) {
            reply = this.getMessageFactory().createMethodReturn(_methodCall, null, new Object[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : Marshalling.getDBusType(_me.getGenericReturnType())) {
                sb.append(s);
            }
            Object[] nr = Marshalling.convertParameters(new Object[]{_result}, new Type[]{_me.getGenericReturnType()}, this);
            reply = this.getMessageFactory().createMethodReturn(_methodCall, sb.toString(), nr);
        }
        this.sendMessage(reply);
    }

    protected Object invokeMethod(MethodCall _methodCall, Method _me, Object _ob) throws Throwable {
        DBusCallInfo info = new DBusCallInfo(_methodCall);
        this.getInfoMap().put(Thread.currentThread(), info);
        try {
            LoggingHelper.logIf(this.getLogger().isTraceEnabled(), () -> {
                try {
                    Object[] params4 = _methodCall.getParameters();
                    this.getLogger().trace("Invoking Method: {} on {} with parameters {}", _me, _ob, Arrays.deepToString(params4));
                }
                catch (DBusException _ex) {
                    this.getLogger().trace("Error getting parameters from method call", _ex);
                }
            });
            Object[] params5 = _methodCall.getParameters();
            Object object = _me.invoke(_ob, params5);
            return object;
        }
        catch (InvocationTargetException _ex) {
            this.getLogger().debug(_ex.getMessage(), _ex);
            throw _ex.getCause();
        }
        finally {
            this.getInfoMap().remove(Thread.currentThread());
        }
    }
}

