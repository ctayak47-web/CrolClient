
package org.freedesktop.dbus.connections.base;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.freedesktop.dbus.DBusAsyncReply;
import org.freedesktop.dbus.DBusCallInfo;
import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.MethodTuple;
import org.freedesktop.dbus.RemoteInvocationHandler;
import org.freedesktop.dbus.annotations.DBusProperties;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.connections.base.AbstractConnectionBase;
import org.freedesktop.dbus.connections.base.ConnectionMethodInvocation;
import org.freedesktop.dbus.connections.base.GlobalHandler;
import org.freedesktop.dbus.connections.config.ReceivingServiceConfig;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.errors.InvalidMethodArgument;
import org.freedesktop.dbus.errors.UnknownMethod;
import org.freedesktop.dbus.errors.UnknownObject;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.CallbackHandler;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.messages.Error;
import org.freedesktop.dbus.messages.ExportedObject;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MethodCall;
import org.freedesktop.dbus.messages.MethodReturn;
import org.freedesktop.dbus.propertyref.PropertyRef;
import org.freedesktop.dbus.types.Variant;
import org.freedesktop.dbus.utils.Util;

/*
 * Uses 'sealed' constructs - enablewith --sealed true
 */
public abstract class ConnectionMessageHandler
extends ConnectionMethodInvocation {
    protected ConnectionMessageHandler(TransportConfig _transportConfig, ReceivingServiceConfig _rsCfg) throws DBusException {
        super(_transportConfig, _rsCfg);
    }

    @Override
    protected void handleException(Message _methodOrSignal, DBusExecutionException _exception) {
        try {
            this.sendMessage(this.getMessageFactory().createError(_methodOrSignal, _exception));
        }
        catch (DBusException _ex) {
            this.getLogger().warn("Exception caught while processing previous error.", _ex);
        }
    }

    private void handleMessage(DBusSignal _signal, boolean _useThreadPool) {
        Runnable command;
        this.getLogger().debug("Handling incoming signal: {}", (Object)_signal);
        ArrayList handlers = new ArrayList();
        ArrayList genericHandlers = new ArrayList();
        for (Map.Entry<DBusMatchRule, Queue<DBusSigHandler<? extends DBusSignal>>> entry : this.getHandledSignals().entrySet()) {
            if (!entry.getKey().matches(_signal, false)) continue;
            handlers.addAll(entry.getValue());
        }
        for (Map.Entry<DBusMatchRule, Queue<DBusSigHandler<? extends DBusSignal>>> entry : this.getGenericHandledSignals().entrySet()) {
            if (!entry.getKey().matches(_signal, false)) continue;
            genericHandlers.addAll(entry.getValue());
        }
        if (handlers.isEmpty() && genericHandlers.isEmpty()) {
            return;
        }
        ConnectionMessageHandler conn = this;
        for (DBusSigHandler h : handlers) {
            this.getLogger().trace("Adding Runnable for signal {} with handler {}", (Object)_signal, (Object)h);
            command = () -> {
                try {
                    DBusSignal rs = _signal.getClass().equals(DBusSignal.class) ? _signal.createReal(conn) : _signal;
                    if (rs == null) {
                        return;
                    }
                    h.handle(rs);
                }
                catch (DBusException _ex) {
                    this.getLogger().warn("Exception while running signal handler '{}' for signal '{}':", h, _signal, _ex);
                    this.handleException(_signal, new DBusExecutionException("Error handling signal " + _signal.getInterface() + "." + _signal.getName() + ": " + _ex.getMessage()));
                }
            };
            if (_useThreadPool) {
                this.getReceivingService().execSignalHandler(command);
                continue;
            }
            command.run();
        }
        for (DBusSigHandler h : genericHandlers) {
            this.getLogger().trace("Adding Runnable for signal {} with handler {}", (Object)_signal, (Object)h);
            command = () -> h.handle(_signal);
            if (_useThreadPool) {
                this.getReceivingService().execSignalHandler(command);
                continue;
            }
            command.run();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void handleMessage(final Error _err) {
        this.getLogger().debug("Handling incoming error: {}", (Object)_err);
        MethodCall m = null;
        if (this.getPendingCalls() == null) {
            return;
        }
        Map<Long, MethodCall> map = this.getPendingCalls();
        synchronized (map) {
            if (this.getPendingCalls().containsKey(_err.getReplySerial())) {
                m = this.getPendingCalls().remove(_err.getReplySerial());
            }
        }
        if (m != null) {
            m.setReply(_err);
            CallbackHandler<? extends Object> cbh = this.getCallbackManager().removeCallback(m);
            this.getLogger().trace("{} = pendingCallbacks.remove({})", (Object)cbh, (Object)m);
            if (null != cbh) {
                final CallbackHandler<? extends Object> fcbh = cbh;
                this.getLogger().trace("Adding Error Runnable with callback handler {}", (Object)fcbh);
                Runnable command = new Runnable(){

                    @Override
                    public synchronized void run() {
                        try {
                            ConnectionMessageHandler.this.getLogger().trace("Running Error Callback for {}", (Object)_err);
                            DBusCallInfo info = new DBusCallInfo(_err);
                            ConnectionMessageHandler.this.getInfoMap().put(Thread.currentThread(), info);
                            fcbh.handleError(_err.getException());
                            ConnectionMessageHandler.this.getInfoMap().remove(Thread.currentThread());
                        }
                        catch (Exception _ex) {
                            ConnectionMessageHandler.this.getLogger().debug("Exception while running error callback.", _ex);
                        }
                    }
                };
                this.getReceivingService().execErrorHandler(command);
            }
        } else {
            this.getPendingErrorQueue().add(_err);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void handleMessage(final MethodReturn _mr) {
        this.getLogger().debug("Handling incoming method return: {}", (Object)_mr);
        MethodCall m = null;
        if (null == this.getPendingCalls()) {
            return;
        }
        Map<Long, MethodCall> map = this.getPendingCalls();
        synchronized (map) {
            if (this.getPendingCalls().containsKey(_mr.getReplySerial())) {
                m = this.getPendingCalls().remove(_mr.getReplySerial());
            }
        }
        if (null != m) {
            m.setReply(_mr);
            _mr.setCall(m);
            CallbackHandler<? extends Object> cbh = this.getCallbackManager().getCallback(m);
            DBusAsyncReply<?> asr = this.getCallbackManager().getCallbackReply(m);
            this.getCallbackManager().removeCallback(m);
            if (null != cbh) {
                final CallbackHandler<? extends Object> fcbh = cbh;
                final DBusAsyncReply<?> fasr = asr;
                if (fasr == null) {
                    this.getLogger().debug("Cannot add runnable for method, given method callback was null");
                    return;
                }
                this.getLogger().trace("Adding Runnable for method {} with callback handler {}", (Object)fcbh, (Object)fasr.getMethod());
                Runnable r = new Runnable(){

                    @Override
                    public synchronized void run() {
                        try {
                            ConnectionMessageHandler.this.getLogger().trace("Running Callback for {}", (Object)_mr);
                            DBusCallInfo info = new DBusCallInfo(_mr);
                            ConnectionMessageHandler.this.getInfoMap().put(Thread.currentThread(), info);
                            Object convertRV = RemoteInvocationHandler.convertRV(_mr.getParameters(), fasr.getMethod(), fasr.getConnection());
                            fcbh.handle(convertRV);
                            ConnectionMessageHandler.this.getInfoMap().remove(Thread.currentThread());
                        }
                        catch (Exception _ex) {
                            ConnectionMessageHandler.this.getLogger().debug("Exception while running callback.", _ex);
                        }
                    }
                };
                this.getReceivingService().execMethodReturnHandler(r);
            }
        } else {
            try {
                this.sendMessage(this.getMessageFactory().createError(_mr, new DBusExecutionException("Spurious reply. No message with the given serial id was awaiting a reply.")));
            }
            catch (DBusException _exDe) {
                this.getLogger().trace("Could not send error message", _exDe);
            }
        }
    }

    void handleMessage(Message _message) throws DBusException {
        if (_message instanceof DBusSignal) {
            DBusSignal sig = (DBusSignal)_message;
            this.handleMessage(sig, true);
        } else if (_message instanceof MethodCall) {
            MethodCall mc = (MethodCall)_message;
            this.handleMessage(mc);
        } else if (_message instanceof MethodReturn) {
            MethodReturn mr = (MethodReturn)_message;
            this.handleMessage(mr);
        } else if (_message instanceof Error) {
            Error err = (Error)_message;
            this.handleMessage(err);
        }
    }

    private void handleMessage(MethodCall _methodCall) throws DBusException {
        ExportedObject exportObject;
        this.getLogger().debug("Handling incoming method call: {}", (Object)_methodCall);
        Method meth = null;
        DBusInterface o = null;
        if (null == _methodCall.getInterface() || _methodCall.getInterface().equals("org.freedesktop.DBus.Peer") || _methodCall.getInterface().equals("org.freedesktop.DBus.Introspectable")) {
            exportObject = this.getExportedObjects().get(null);
            if (null != exportObject && null == exportObject.getObject().get()) {
                this.unExportObject(null);
                exportObject = null;
            }
            if (exportObject != null) {
                meth = exportObject.getMethods().get(new MethodTuple(_methodCall.getName(), _methodCall.getSig()));
            }
            if (meth != null) {
                o = new GlobalHandler(this, _methodCall.getPath());
            }
        }
        if (o == null) {
            Object[] params;
            exportObject = this.getExportedObjects().get(_methodCall.getPath());
            this.getLogger().debug("Found exported object: {}", exportObject == null ? "<no object found>" : exportObject);
            if (exportObject != null && exportObject.getObject().get() == null) {
                this.getLogger().info("Unexporting {} implicitly (object present: {}, reference present: {})", _methodCall.getPath(), exportObject != null, exportObject.getObject().get() == null);
                this.unExportObject(_methodCall.getPath());
                exportObject = null;
            }
            if (exportObject == null) {
                exportObject = this.getFallbackContainer().get(_methodCall.getPath());
                this.getLogger().debug("Found {} in fallback container", exportObject == null ? "no" : exportObject);
            }
            if (exportObject == null) {
                this.getLogger().debug("No object found for method {}", (Object)_methodCall.getPath());
                this.sendMessage(this.getMessageFactory().createError(_methodCall, new UnknownObject(_methodCall.getPath() + " is not an object provided by this process.")));
                return;
            }
            if (this.getLogger().isTraceEnabled()) {
                this.getLogger().trace("Searching for method {}  with signature {}", (Object)_methodCall.getName(), (Object)_methodCall.getSig());
                this.getLogger().trace("List of methods on {}: ", (Object)exportObject);
                for (MethodTuple mt : exportObject.getMethods().keySet()) {
                    this.getLogger().trace("   {} => {}", (Object)mt, (Object)exportObject.getMethods().get(mt));
                }
            }
            if (this.handleDBusBoundProperties(exportObject, _methodCall, params = _methodCall.getParameters())) {
                return;
            }
            if (meth == null && null == (meth = exportObject.getMethods().get(new MethodTuple(_methodCall.getName(), _methodCall.getSig())))) {
                this.sendMessage(this.getMessageFactory().createError(_methodCall, new UnknownMethod(String.format("The method `%s.%s' does not exist on this object.", _methodCall.getInterface(), _methodCall.getName()))));
                return;
            }
            o = exportObject.getObject().get();
        }
        if (ExportedObject.isExcluded(meth)) {
            this.sendMessage(this.getMessageFactory().createError(_methodCall, new UnknownMethod(String.format("The method `%s.%s' is not exported.", _methodCall.getInterface(), _methodCall.getName()))));
            return;
        }
        this.queueInvokeMethod(_methodCall, meth, o);
    }

    private boolean handleDBusBoundProperties(ExportedObject _exportObject, MethodCall _methodCall, Object[] _params) throws DBusException {
        Set<Map.Entry<PropertyRef, Method>> allPropertyMethods;
        if (_params.length == 2 && _params[0] instanceof String && _params[1] instanceof String && _methodCall.getName().equals("Get")) {
            PropertyRef propertyRef = new PropertyRef((String)_params[1], null, DBusProperty.Access.READ);
            Method propMeth = _exportObject.getPropertyMethods().get(propertyRef);
            if (propMeth != null) {
                DBusInterface object = _exportObject.getObject().get();
                this.getReceivingService().execMethodCallHandler(() -> {
                    _methodCall.setArgs(new Object[0]);
                    this.invokeMethodAndReply(_methodCall, propMeth, object, 1 == (_methodCall.getFlags() & 1));
                });
                return true;
            }
        } else if (_params.length == 3 && _params[0] instanceof String && _params[1] instanceof String && _methodCall.getName().equals("Set")) {
            PropertyRef propertyRef = new PropertyRef((String)_params[1], null, DBusProperty.Access.WRITE);
            Method propMeth = _exportObject.getPropertyMethods().get(propertyRef);
            if (propMeth != null) {
                DBusInterface object = _exportObject.getObject().get();
                Class<?> type = PropertyRef.typeForMethod(propMeth);
                AtomicBoolean isVariant = new AtomicBoolean(false);
                Object val = Optional.ofNullable(_params[2]).map(v -> {
                    if (v instanceof Variant) {
                        Variant va = (Variant)v;
                        isVariant.set(true);
                        return va.getValue();
                    }
                    return v;
                }).orElse(null);
                this.getReceivingService().execMethodCallHandler(() -> {
                    try {
                        AbstractCollection myVal = val;
                        Parameter[] parameters = propMeth.getParameters();
                        if (parameters.length != 1) {
                            throw new InvalidMethodArgument("Expected method with one argument, but found " + parameters.length);
                        }
                        if (Collection.class.isAssignableFrom(parameters[0].getType()) && isVariant.get() && myVal != null && myVal.getClass().isArray()) {
                            myVal = Set.class.isAssignableFrom(parameters[0].getType()) ? new LinkedHashSet<Object>(Arrays.asList(Util.toObjectArray(myVal))) : new ArrayList<Object>(Arrays.asList(Util.toObjectArray(myVal)));
                        }
                        _methodCall.setArgs(Marshalling.deSerializeParameters(new Object[]{myVal}, new Type[]{type}, (AbstractConnectionBase)this));
                        this.invokeMethodAndReply(_methodCall, propMeth, object, 1 == (_methodCall.getFlags() & 1));
                    }
                    catch (Exception _ex) {
                        this.getLogger().debug("Failed to invoke method call on Properties", _ex);
                        this.handleException(_methodCall, new UnknownMethod("Failure in de-serializing message: " + String.valueOf(_ex)));
                        return;
                    }
                });
                return true;
            }
        } else if (_params.length == 1 && _params[0] instanceof String && _methodCall.getName().equals("GetAll") && !(allPropertyMethods = _exportObject.getPropertyMethods().entrySet()).isEmpty()) {
            DBusInterface object = _exportObject.getObject().get();
            Method meth = null;
            if (object instanceof DBusProperties) {
                meth = _exportObject.getMethods().get(new MethodTuple(_methodCall.getName(), _methodCall.getSig()));
                if (null == meth) {
                    this.sendMessage(this.getMessageFactory().createError(_methodCall, new UnknownMethod(String.format("The method `%s.%s' does not exist on this object.", _methodCall.getInterface(), _methodCall.getName()))));
                    return true;
                }
            } else {
                try {
                    meth = Properties.class.getDeclaredMethod("GetAll", String.class);
                }
                catch (NoSuchMethodException | SecurityException _ex) {
                    this.getLogger().debug("Properties GetAll failed", _ex);
                    this.handleException(_methodCall, new DBusExecutionException(String.format("Error Executing Method %s.%s: %s", _methodCall.getInterface(), _methodCall.getName(), _ex.getMessage())));
                }
            }
            Method originalMeth = meth;
            this.getReceivingService().execMethodCallHandler(() -> {
                HashMap<String, Object> resultMap = new HashMap<String, Object>();
                for (Map.Entry propEn : allPropertyMethods) {
                    Method propMeth = (Method)propEn.getValue();
                    if (((PropertyRef)propEn.getKey()).getAccess() != DBusProperty.Access.READ) continue;
                    try {
                        _methodCall.setArgs(new Object[0]);
                        Object val = this.invokeMethod(_methodCall, propMeth, object);
                        resultMap.put(((PropertyRef)propEn.getKey()).getName(), val);
                    }
                    catch (Throwable _ex) {
                        this.getLogger().debug("", _ex);
                        this.handleException(_methodCall, new UnknownMethod("Failure in de-serializing message: " + String.valueOf(_ex)));
                        return;
                    }
                }
                if (object instanceof DBusProperties) {
                    resultMap.putAll((Map)this.setupAndInvoke(_methodCall, originalMeth, object, true));
                }
                try {
                    this.invokedMethodReply(_methodCall, originalMeth, resultMap);
                }
                catch (DBusExecutionException _ex) {
                    this.getLogger().debug("Error invoking method call", _ex);
                    this.handleException(_methodCall, _ex);
                }
                catch (Throwable _ex) {
                    this.getLogger().debug("Failed to invoke method call", _ex);
                    this.handleException(_methodCall, new DBusExecutionException(String.format("Error Executing Method %s.%s: %s", _methodCall.getInterface(), _methodCall.getName(), _ex.getMessage())));
                }
            });
            return true;
        }
        return false;
    }
}

