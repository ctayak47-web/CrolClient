
package org.freedesktop.dbus.spi.message;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.MessageProtocolVersionException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MessageFactory;
import org.freedesktop.dbus.spi.message.IMessageReader;
import org.freedesktop.dbus.spi.message.ISocketProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInputStreamMessageReader
implements IMessageReader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int[] len;
    private final byte[] buf;
    private final byte[] tbuf;
    private final SocketChannel inputChannel;
    private byte[] header;
    private byte[] body;
    private final ISocketProvider socketProviderImpl;

    protected AbstractInputStreamMessageReader(SocketChannel _in, ISocketProvider _socketProviderImpl) {
        this.socketProviderImpl = Objects.requireNonNull(_socketProviderImpl, "ISocketProvider implementation required");
        this.inputChannel = Objects.requireNonNull(_in, "SocketChannel required");
        this.len = new int[4];
        this.tbuf = new byte[4];
        this.buf = new byte[12];
        this.len[1] = 0;
        this.len[0] = 0;
    }

    @Override
    public final Message readMessage() throws IOException, DBusException {
        int headerlen;
        int rv;
        if (this.len[0] < 12) {
            try {
                ByteBuffer wrapBuf = ByteBuffer.wrap(this.buf, this.len[0], 12 - this.len[0]);
                rv = this.inputChannel.read(wrapBuf);
                if (rv < 0) {
                    throw new EOFException("(1) Underlying transport returned " + rv);
                }
                this.len[0] = this.len[0] + rv;
            }
            catch (SocketTimeoutException _ex) {
                return null;
            }
        }
        if (this.len[0] == 0) {
            return null;
        }
        if (this.len[0] < 12) {
            this.logger.trace("Only got {} of 12 bytes of header", (Object)this.len[0]);
            return null;
        }
        byte protoVer = this.buf[3];
        if (protoVer > 1) {
            throw new MessageProtocolVersionException(String.format("Protocol version %s is unsupported", protoVer));
        }
        if (this.len[1] < 4) {
            try {
                rv = this.inputChannel.read(ByteBuffer.wrap(this.tbuf, this.len[1], 4 - this.len[1]));
                if (rv < 0) {
                    throw new EOFException("(2) Underlying transport returned " + rv);
                }
                this.len[1] = this.len[1] + rv;
            }
            catch (SocketTimeoutException _ex) {
                return null;
            }
        }
        if (this.len[1] < 4) {
            this.logger.trace("Only got {} of 4 bytes of header", (Object)this.len[1]);
            return null;
        }
        byte endian = this.buf[0];
        if (this.header == null) {
            headerlen = (int)Message.demarshallint(this.tbuf, 0, endian, 4);
            int modlen = headerlen & 7;
            if (modlen != 0) {
                headerlen += 8 - modlen;
            }
        } else {
            headerlen = this.header.length - 8;
        }
        if (this.header == null) {
            this.header = new byte[headerlen + 8];
            System.arraycopy(this.tbuf, 0, this.header, 0, 4);
            this.len[2] = 0;
        }
        if (this.len[2] < headerlen) {
            try {
                int rv2 = this.inputChannel.read(ByteBuffer.wrap(this.header, 8 + this.len[2], headerlen - this.len[2]));
                if (rv2 < 0) {
                    throw new EOFException("(3) Underlying transport returned " + rv2);
                }
                this.len[2] = this.len[2] + rv2;
            }
            catch (SocketTimeoutException _ex) {
                return null;
            }
        }
        if (this.len[2] < headerlen) {
            this.logger.trace("Only got {} of {} bytes of header", (Object)this.len[2], (Object)headerlen);
            return null;
        }
        byte type = this.buf[1];
        if (this.body == null) {
            this.body = new byte[(int)Message.demarshallint(this.buf, 4, endian, 4)];
            this.len[3] = 0;
        }
        if (this.len[3] < this.body.length) {
            try {
                int rv3 = this.inputChannel.read(ByteBuffer.wrap(this.body, this.len[3], this.body.length - this.len[3]));
                if (rv3 < 0) {
                    throw new EOFException("(4) Underlying transport returned " + rv3);
                }
                this.len[3] = this.len[3] + rv3;
            }
            catch (SocketTimeoutException _ex) {
                return null;
            }
        }
        if (this.len[3] < this.body.length) {
            this.logger.trace("Only got {} of {} bytes of body", (Object)this.len[3], (Object)this.body.length);
            return null;
        }
        try {
            List<FileDescriptor> fds = null;
            if (this.socketProviderImpl.isFileDescriptorPassingSupported()) {
                fds = this.readFileDescriptors(this.inputChannel);
            }
            Message m = MessageFactory.createMessage(type, this.buf, this.header, this.body, fds);
            this.logger.debug("=> {}", (Object)m);
            Message message = m;
            return message;
        }
        catch (RuntimeException | DBusException _ex) {
            this.logger.warn("Exception while creating message.", _ex);
            throw _ex;
        }
        finally {
            Arrays.fill(this.tbuf, (byte)0);
            this.len[1] = 0;
            this.body = null;
            this.header = null;
            Arrays.fill(this.buf, (byte)0);
            this.len[0] = 0;
        }
    }

    protected abstract List<FileDescriptor> readFileDescriptors(SocketChannel var1) throws DBusException;

    protected Logger getLogger() {
        return this.logger;
    }

    protected ISocketProvider getSocketProviderImpl() {
        return this.socketProviderImpl;
    }

    @Override
    public void close() throws IOException {
        if (this.inputChannel.isOpen()) {
            this.logger.trace("Closing Message Reader");
            this.inputChannel.close();
        }
    }

    @Override
    public boolean isClosed() {
        return !this.inputChannel.isOpen();
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [inputChannel=" + String.valueOf(this.inputChannel) + ", socketProviderImpl=" + String.valueOf(this.socketProviderImpl) + "]";
    }
}

