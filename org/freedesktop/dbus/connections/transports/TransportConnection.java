
package org.freedesktop.dbus.connections.transports;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;
import org.freedesktop.dbus.messages.MessageFactory;
import org.freedesktop.dbus.spi.message.IMessageReader;
import org.freedesktop.dbus.spi.message.IMessageWriter;
import org.freedesktop.dbus.spi.message.ISocketProvider;

public class TransportConnection
implements Closeable {
    private static final AtomicLong TRANSPORT_ID_GENERATOR = new AtomicLong(0L);
    private final long id = TRANSPORT_ID_GENERATOR.incrementAndGet();
    private final SocketChannel channel;
    private final IMessageWriter writer;
    private final IMessageReader reader;
    private final ISocketProvider socketProviderImpl;
    private final MessageFactory messageFactory;

    public TransportConnection(MessageFactory _factory, SocketChannel _channel, ISocketProvider _socketProviderImpl, IMessageWriter _writer, IMessageReader _reader) {
        this.messageFactory = _factory;
        this.channel = _channel;
        this.socketProviderImpl = _socketProviderImpl;
        this.writer = _writer;
        this.reader = _reader;
    }

    public SocketChannel getChannel() {
        return this.channel;
    }

    public IMessageWriter getWriter() {
        return this.writer;
    }

    public IMessageReader getReader() {
        return this.reader;
    }

    public ISocketProvider getSocketProviderImpl() {
        return this.socketProviderImpl;
    }

    public long getId() {
        return this.id;
    }

    public MessageFactory getMessageFactory() {
        return this.messageFactory;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [id=" + this.id + ", channel=" + String.valueOf(this.channel) + ", writer=" + String.valueOf(this.writer) + ", reader=" + String.valueOf(this.reader) + "]";
    }

    @Override
    public void close() throws IOException {
        if (this.reader != null) {
            this.reader.close();
        }
        if (this.writer != null) {
            this.writer.close();
        }
        if (this.channel != null) {
            this.channel.close();
        }
    }
}

