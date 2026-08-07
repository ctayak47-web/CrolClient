
package org.freedesktop.dbus.messages;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.types.UInt32;

public abstract class MethodBase
extends Message {
    MethodBase() {
    }

    protected MethodBase(byte _endianness, byte _methodCall, byte _flags) throws DBusException {
        super(_endianness, _methodCall, _flags);
    }

    void appendFileDescriptors(List<Object> _hargs, Object ... _args) {
        long totalFileDes;
        Objects.requireNonNull(_hargs);
        long l = totalFileDes = _args == null ? 0L : Arrays.stream(_args).filter(FileDescriptor.class::isInstance).count();
        if (totalFileDes > 0L) {
            _hargs.add(this.createHeaderArgs((byte)9, "u", new UInt32(totalFileDes)));
        }
    }
}

