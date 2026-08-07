
package jnr.unixsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import jnr.unixsocket.Credentials;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import jnr.unixsocket.UnixSocketOptions;

public class UnixSocket
extends Socket {
    private UnixSocketChannel chan;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicBoolean indown = new AtomicBoolean(false);
    private AtomicBoolean outdown = new AtomicBoolean(false);
    private InputStream in;
    private OutputStream out;

    public UnixSocket(UnixSocketChannel chan) {
        this.chan = chan;
        this.in = Channels.newInputStream(new UnselectableByteChannel(chan));
        this.out = Channels.newOutputStream(new UnselectableByteChannel(chan));
    }

    @Override
    public void bind(SocketAddress local) throws IOException {
        if (null != this.chan) {
            if (this.isClosed()) {
                throw new SocketException("Socket is closed");
            }
            if (this.isBound()) {
                throw new SocketException("already bound");
            }
            try {
                this.chan.bind(local);
            }
            catch (IOException e) {
                throw (SocketException)new SocketException().initCause(e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (null != this.chan && this.closed.compareAndSet(false, true)) {
            try {
                this.chan.close();
            }
            catch (IOException e) {
                this.ignore();
            }
        }
    }

    @Override
    public void connect(SocketAddress addr) throws IOException {
        this.connect(addr, 0);
    }

    @Override
    public void connect(SocketAddress addr, int timeout) throws IOException {
        if (!(addr instanceof UnixSocketAddress)) {
            throw new IllegalArgumentException("address of type " + addr.getClass() + " are not supported. Use " + UnixSocketAddress.class + " instead");
        }
        this.chan.connect((UnixSocketAddress)addr);
    }

    @Override
    public SocketChannel getChannel() {
        return this.chan;
    }

    @Override
    public InetAddress getInetAddress() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (this.chan.isConnected()) {
            return this.in;
        }
        throw new IOException("not connected");
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return this.chan.getLocalSocketAddress();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (this.chan.isConnected()) {
            return this.out;
        }
        throw new IOException("not connected");
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        UnixSocketAddress address = this.chan.getRemoteSocketAddress();
        if (address != null) {
            return address;
        }
        return null;
    }

    @Override
    public boolean isBound() {
        if (null == this.chan) {
            return false;
        }
        return this.chan.isBound();
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

    @Override
    public boolean isConnected() {
        return this.chan.isConnected();
    }

    @Override
    public boolean isInputShutdown() {
        return this.indown.get();
    }

    @Override
    public boolean isOutputShutdown() {
        return this.outdown.get();
    }

    @Override
    public void shutdownInput() throws IOException {
        if (this.indown.compareAndSet(false, true)) {
            this.chan.shutdownInput();
        }
    }

    @Override
    public void shutdownOutput() throws IOException {
        if (this.outdown.compareAndSet(false, true)) {
            this.chan.shutdownOutput();
        }
    }

    public final Credentials getCredentials() throws SocketException {
        if (!this.chan.isConnected()) {
            return null;
        }
        try {
            return this.chan.getOption(UnixSocketOptions.SO_PEERCRED);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        try {
            return this.chan.getOption(UnixSocketOptions.SO_KEEPALIVE);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        try {
            return this.chan.getOption(UnixSocketOptions.SO_RCVBUF);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        try {
            return this.chan.getOption(UnixSocketOptions.SO_SNDBUF);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getSoTimeout() throws SocketException {
        try {
            return this.chan.getOption(UnixSocketOptions.SO_RCVTIMEO);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        try {
            this.chan.setOption((SocketOption)UnixSocketOptions.SO_KEEPALIVE, (Object)on);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        try {
            this.chan.setOption((SocketOption)UnixSocketOptions.SO_RCVBUF, (Object)size);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        try {
            this.chan.setOption((SocketOption)UnixSocketOptions.SO_SNDBUF, (Object)size);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        try {
            this.chan.setOption((SocketOption)UnixSocketOptions.SO_RCVTIMEO, (Object)timeout);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    private void ignore() {
    }

    static final class UnselectableByteChannel
    implements ReadableByteChannel,
    WritableByteChannel {
        private final UnixSocketChannel channel;

        UnselectableByteChannel(UnixSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return this.channel.write(src);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            return this.channel.read(dst);
        }

        @Override
        public boolean isOpen() {
            return this.channel.isOpen();
        }

        @Override
        public void close() throws IOException {
            this.channel.close();
        }
    }
}

