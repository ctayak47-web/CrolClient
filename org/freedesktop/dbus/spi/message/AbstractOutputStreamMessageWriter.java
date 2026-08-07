
package org.freedesktop.dbus.spi.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Objects;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.spi.message.IMessageWriter;
import org.freedesktop.dbus.spi.message.ISocketProvider;
import org.freedesktop.dbus.utils.Hexdump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOutputStreamMessageWriter
implements IMessageWriter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SocketChannel outputChannel;
    private final ISocketProvider socketProviderImpl;

    protected AbstractOutputStreamMessageWriter(SocketChannel _out, ISocketProvider _socketProviderImpl) {
        this.outputChannel = Objects.requireNonNull(_out, "SocketChannel required");
        this.socketProviderImpl = Objects.requireNonNull(_socketProviderImpl, "ISocketProvider implementation required");
    }

    @Override
    public final void writeMessage(Message _msg) throws IOException {
        this.logger.debug("<= {}", (Object)_msg);
        if (null == _msg) {
            return;
        }
        if (null == _msg.getWireData()) {
            this.logger.warn("Message {} wire-data was null!", (Object)_msg);
            return;
        }
        if (this.socketProviderImpl.isFileDescriptorPassingSupported()) {
            this.writeFileDescriptors(this.outputChannel, _msg.getFiledescriptors());
        }
        for (byte[] buf : _msg.getWireData()) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("{}", (Object)(null == buf ? "(buffer was null)" : Hexdump.format(buf)));
            }
            if (null == buf) break;
            this.outputChannel.write(ByteBuffer.wrap(buf));
        }
        this.logger.trace("Message sent: {}", (Object)_msg);
    }

    protected abstract void writeFileDescriptors(SocketChannel var1, List<FileDescriptor> var2) throws IOException;

    protected Logger getLogger() {
        return this.logger;
    }

    protected ISocketProvider getSocketProviderImpl() {
        return this.socketProviderImpl;
    }

    @Override
    public void close() throws IOException {
        this.logger.debug("Closing Message Writer");
        if (this.outputChannel.isOpen()) {
            this.outputChannel.close();
            this.logger.debug("Message Writer closed");
        }
    }

    @Override
    public boolean isClosed() {
        return !this.outputChannel.isOpen();
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [outputChannel=" + String.valueOf(this.outputChannel) + ", socketProviderImpl=" + String.valueOf(this.socketProviderImpl) + "]";
    }
}

