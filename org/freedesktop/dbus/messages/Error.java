
package org.freedesktop.dbus.messages;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.exceptions.MessageFormatException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.utils.CommonRegexPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Error
extends Message {
    private static final String DEFAULT_NULL_EXCEPTION_ERROR_MSG = "Unsupported NULL Exception";
    private static final Logger LOGGER = LoggerFactory.getLogger(Error.class);

    protected Error() {
    }

    protected Error(byte _endianess, String _dest, String _errorName, long _replyserial, String _sig, Object ... _args) throws DBusException {
        this(_endianess, null, _dest, _errorName, _replyserial, _sig, _args);
    }

    protected Error(byte _endianess, String _source, String _dest, String _errorName, long _replyserial, String _sig, Object ... _args) throws DBusException {
        super(_endianess, (byte)3, (byte)0);
        if (null == _errorName) {
            throw new MessageFormatException("Must specify error name to Errors.");
        }
        ArrayList<Object> hargs = new ArrayList<Object>();
        hargs.add(this.createHeaderArgs((byte)4, "s", _errorName));
        hargs.add(this.createHeaderArgs((byte)5, "u", _replyserial));
        if (null != _source) {
            hargs.add(this.createHeaderArgs((byte)7, "s", _source));
        }
        if (null != _dest) {
            hargs.add(this.createHeaderArgs((byte)6, "s", _dest));
        }
        if (null != _sig) {
            hargs.add(this.createHeaderArgs((byte)8, "g", _sig));
            this.setArgs(_args);
        }
        this.padAndMarshall(hargs, this.getSerial(), _sig, _args);
    }

    protected Error(byte _endianess, String _source, Message _m, Throwable _ex) throws DBusException {
        this(_endianess, _source, _m.getSource(), AbstractConnection.DOLLAR_PATTERN.matcher(Optional.ofNullable(_ex).orElse(new IOException(DEFAULT_NULL_EXCEPTION_ERROR_MSG)).getClass().getName()).replaceAll("."), _m.getSerial(), "s", _ex == null ? DEFAULT_NULL_EXCEPTION_ERROR_MSG : _ex.getMessage());
    }

    protected Error(byte _endianess, Message _m, Throwable _ex) throws DBusException {
        this(_endianess, _m.getSource(), AbstractConnection.DOLLAR_PATTERN.matcher(Optional.ofNullable(_ex).orElse(new IOException(DEFAULT_NULL_EXCEPTION_ERROR_MSG)).getClass().getName()).replaceAll("."), _m.getSerial(), "s", _ex == null ? DEFAULT_NULL_EXCEPTION_ERROR_MSG : _ex.getMessage());
    }

    private static Class<? extends DBusExecutionException> createExceptionClass(String _name) {
        Class<?> c = null;
        String name = _name;
        if (name.startsWith("org.freedesktop.DBus.Error.")) {
            name = name.replace("org.freedesktop.DBus.Error.", "org.freedesktop.dbus.errors.");
        }
        do {
            try {
                c = Class.forName(name);
            }
            catch (ClassNotFoundException _exCnf) {
                LOGGER.trace("Could not find class for name {}", (Object)name, (Object)_exCnf);
            }
            name = CommonRegexPattern.EXCEPTION_EXTRACT_PATTERN.matcher(name).replaceAll("\\$$1");
        } while (null == c && CommonRegexPattern.EXCEPTION_PARTIAL_PATTERN.matcher(name).matches());
        return c;
    }

    public DBusExecutionException getException() {
        try {
            Class<? extends DBusExecutionException> c = Error.createExceptionClass(this.getName());
            if (null == c || !DBusExecutionException.class.isAssignableFrom(c)) {
                c = DBusExecutionException.class;
            }
            Constructor<? extends DBusExecutionException> con = c.getConstructor(String.class);
            Object[] args = this.getParameters();
            DBusExecutionException ex = null == args || 0 == args.length ? con.newInstance("") : con.newInstance(Arrays.stream(args).map(Objects::toString).collect(Collectors.joining(" ")).trim());
            ex.setType(this.getName());
            return ex;
        }
        catch (Exception _ex1) {
            this.logger.debug("", _ex1);
            Object[] args = null;
            try {
                args = this.getParameters();
            }
            catch (Exception _ex2) {
                LOGGER.trace("Cannot retrieve parameters", _ex2);
            }
            DBusExecutionException ex = null == args || 0 == args.length ? new DBusExecutionException("") : new DBusExecutionException(Arrays.stream(args).map(Objects::toString).collect(Collectors.joining(" ")).trim());
            ex.setType(this.getName());
            return ex;
        }
    }

    public void throwException() throws DBusExecutionException {
        throw this.getException();
    }
}

