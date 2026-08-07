
package org.freedesktop.dbus.messages;

import java.util.ArrayList;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.MethodBase;
import org.freedesktop.dbus.messages.MethodCall;

public class MethodReturn
extends MethodBase {
    private MethodCall call;

    MethodReturn() {
    }

    protected MethodReturn(byte _endianess, String _dest, long _replyserial, String _sig, Object ... _args) throws DBusException {
        this(_endianess, null, _dest, _replyserial, _sig, _args);
    }

    protected MethodReturn(byte _endianess, String _source, String _dest, long _replyserial, String _sig, Object ... _args) throws DBusException {
        super(_endianess, (byte)2, (byte)0);
        ArrayList<Object> hargs = new ArrayList<Object>();
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
        this.appendFileDescriptors(hargs, _args);
        this.padAndMarshall(hargs, this.getSerial(), _sig, _args);
    }

    protected MethodReturn(MethodCall _mc, String _sig, Object ... _args) throws DBusException {
        this(null, _mc, _sig, _args);
    }

    protected MethodReturn(String _source, MethodCall _mc, String _sig, Object ... _args) throws DBusException {
        this(_mc.getEndianess(), _source, _mc.getSource(), _mc.getSerial(), _sig, _args);
        this.call = _mc;
    }

    public MethodCall getCall() {
        return this.call;
    }

    public void setCall(MethodCall _call) {
        this.call = _call;
    }
}

