
package org.freedesktop.dbus.messages;

import java.time.Duration;
import java.util.ArrayList;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.MessageFormatException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MethodBase;

public class MethodCall
extends MethodBase {
    private static long replyWaitTimeout = Duration.ofSeconds(20L).toMillis();
    Message reply = null;

    MethodCall() {
    }

    protected MethodCall(byte _endianess, String _dest, String _path, String _iface, String _member, byte _flags, String _sig, Object ... _args) throws DBusException {
        this(_endianess, null, _dest, _path, _iface, _member, _flags, _sig, _args);
    }

    protected MethodCall(byte _endianess, String _source, String _dest, String _path, String _iface, String _member, byte _flags, String _sig, Object ... _args) throws DBusException {
        super(_endianess, (byte)1, _flags);
        if (null == _member || null == _path) {
            throw new MessageFormatException("Must specify destination, path and function name to MethodCalls.");
        }
        Object[] header = this.getHeader();
        header[1] = _path;
        header[3] = _member;
        ArrayList<Object> hargs = new ArrayList<Object>();
        hargs.add(this.createHeaderArgs((byte)1, "o", _path));
        if (null != _source) {
            hargs.add(this.createHeaderArgs((byte)7, "s", _source));
        }
        if (null != _dest) {
            hargs.add(this.createHeaderArgs((byte)6, "s", _dest));
        }
        if (null != _iface) {
            hargs.add(this.createHeaderArgs((byte)2, "s", _iface));
        }
        hargs.add(this.createHeaderArgs((byte)3, "s", _member));
        if (null != _sig) {
            this.logger.debug("Appending arguments with signature: {}", (Object)_sig);
            hargs.add(this.createHeaderArgs((byte)8, "g", _sig));
            this.setArgs(_args);
        }
        this.appendFileDescriptors(hargs, _args);
        this.padAndMarshall(hargs, this.getSerial(), _sig, _args);
    }

    public static void setDefaultTimeout(long _timeout) {
        replyWaitTimeout = _timeout;
    }

    public synchronized boolean hasReply() {
        return null != this.reply;
    }

    public synchronized Message getReply(long _timeout) {
        this.logger.trace("Blocking on {}", (Object)this);
        if (null != this.reply) {
            return this.reply;
        }
        try {
            this.wait(_timeout);
        }
        catch (InterruptedException _exI) {
            Thread.currentThread().interrupt();
        }
        return this.reply;
    }

    public synchronized Message getReply() {
        return this.getReply(replyWaitTimeout);
    }

    public synchronized void setReply(Message _reply) {
        this.logger.trace("Setting reply to {} to {}", (Object)this, (Object)_reply);
        this.reply = _reply;
        this.notifyAll();
    }
}

