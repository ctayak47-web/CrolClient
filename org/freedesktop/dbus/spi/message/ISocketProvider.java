
package org.freedesktop.dbus.spi.message;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import org.freedesktop.dbus.spi.message.IMessageReader;
import org.freedesktop.dbus.spi.message.IMessageWriter;

public interface ISocketProvider {
    public IMessageReader createReader(SocketChannel var1) throws IOException;

    public IMessageWriter createWriter(SocketChannel var1) throws IOException;

    public void setFileDescriptorSupport(boolean var1);

    public boolean isFileDescriptorPassingSupported();

    default public Optional<Integer> getFileDescriptorValue(FileDescriptor _fd) {
        return Optional.empty();
    }

    default public Optional<FileDescriptor> createFileDescriptor(int _fd) {
        return Optional.empty();
    }
}

