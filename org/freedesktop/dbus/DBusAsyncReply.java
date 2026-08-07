
package org.freedesktop.dbus;

import java.lang.reflect.Method;
import org.freedesktop.dbus.RemoteInvocationHandler;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.errors.NoReply;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.messages.Error;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MethodCall;
import org.freedesktop.dbus.messages.MethodReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBusAsyncReply<T> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private T rval = null;
    private DBusExecutionException error = null;
    private final MethodCall mc;
    private final Method me;
    private final AbstractConnection conn;

    public DBusAsyncReply(MethodCall _mc, Method _me, AbstractConnection _conn) {
        this.mc = _mc;
        this.me = _me;
        this.conn = _conn;
    }

    private synchronized void checkReply() {
        if (this.mc.hasReply()) {
            Message m = this.mc.getReply();
            if (m instanceof Error) {
                Error err = (Error)m;
                this.error = err.getException();
            } else if (m instanceof MethodReturn) {
                try {
                    Object obj = RemoteInvocationHandler.convertRV(m.getParameters(), this.me, this.conn);
                    this.rval = obj;
                }
                catch (DBusExecutionException _ex) {
                    this.error = _ex;
                }
                catch (DBusException _ex) {
                    this.logger.debug("", _ex);
                    this.error = new DBusExecutionException(_ex.getMessage());
                }
            }
        }
    }

    public boolean hasReply() {
        if (null != this.rval || null != this.error) {
            return true;
        }
        this.checkReply();
        return null != this.rval || null != this.error;
    }

    public T getReply() throws DBusException {
        if (null != this.rval) {
            return this.rval;
        }
        if (null != this.error) {
            throw this.error;
        }
        this.checkReply();
        if (null != this.rval) {
            return this.rval;
        }
        if (null != this.error) {
            throw this.error;
        }
        throw new NoReply("Async call has not had a reply");
    }

    public String toString() {
        return "Waiting for: " + String.valueOf(this.mc);
    }

    public Method getMethod() {
        return this.me;
    }

    public AbstractConnection getConnection() {
        return this.conn;
    }

    public MethodCall getCall() {
        return this.mc;
    }
}

