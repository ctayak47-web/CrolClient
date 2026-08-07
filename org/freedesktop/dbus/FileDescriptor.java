
package org.freedesktop.dbus;

import java.util.Optional;
import org.freedesktop.dbus.exceptions.MarshallingException;
import org.freedesktop.dbus.spi.message.ISocketProvider;
import org.freedesktop.dbus.utils.ReflectionFileDescriptorHelper;

public final class FileDescriptor {
    private final int fd;

    public FileDescriptor(int _fd) {
        this.fd = _fd;
    }

    public java.io.FileDescriptor toJavaFileDescriptor(ISocketProvider _provider) throws MarshallingException {
        Optional<java.io.FileDescriptor> result;
        if (_provider != null && (result = _provider.createFileDescriptor(this.fd)).isPresent()) {
            return result.get();
        }
        return (java.io.FileDescriptor)ReflectionFileDescriptorHelper.getInstance().flatMap(helper -> helper.createFileDescriptor(this.fd)).orElseThrow(() -> new MarshallingException("Could not create new FileDescriptor instance"));
    }

    public int getIntFileDescriptor() {
        return this.fd;
    }

    public boolean equals(Object _o) {
        if (this == _o) {
            return true;
        }
        if (_o == null || this.getClass() != _o.getClass()) {
            return false;
        }
        FileDescriptor that = (FileDescriptor)_o;
        return this.fd == that.fd;
    }

    public int hashCode() {
        return this.fd;
    }

    public String toString() {
        return FileDescriptor.class.getSimpleName() + "[fd=" + this.fd + "]";
    }

    public static FileDescriptor fromJavaFileDescriptor(java.io.FileDescriptor _data, ISocketProvider _provider) throws MarshallingException {
        Optional<Integer> result;
        if (_provider != null && (result = _provider.getFileDescriptorValue(_data)).isPresent()) {
            return new FileDescriptor(result.get());
        }
        return new FileDescriptor((Integer)ReflectionFileDescriptorHelper.getInstance().flatMap(helper -> helper.getFileDescriptorValue(_data)).orElseThrow(() -> new MarshallingException("Could not get FileDescriptor value")));
    }
}

