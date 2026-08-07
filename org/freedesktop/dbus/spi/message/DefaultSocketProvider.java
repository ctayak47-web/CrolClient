
package org.freedesktop.dbus.spi.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import org.freedesktop.dbus.spi.message.IMessageReader;
import org.freedesktop.dbus.spi.message.IMessageWriter;
import org.freedesktop.dbus.spi.message.ISocketProvider;
import org.freedesktop.dbus.spi.message.InputStreamMessageReader;
import org.freedesktop.dbus.spi.message.OutputStreamMessageWriter;

final class DefaultSocketProvider
implements ISocketProvider {
    static final ISocketProvider INSTANCE = new DefaultSocketProvider();

    private DefaultSocketProvider() {
    }

    @Override
    public IMessageReader createReader(SocketChannel _socket) throws IOException {
        return new InputStreamMessageReader(_socket);
    }

    @Override
    public IMessageWriter createWriter(SocketChannel _socket) throws IOException {
        return new OutputStreamMessageWriter(_socket);
    }

    @Override
    public void setFileDescriptorSupport(boolean _support) {
    }

    @Override
    public boolean isFileDescriptorPassingSupported() {
        return false;
    }
}

