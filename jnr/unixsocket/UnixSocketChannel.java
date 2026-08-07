
package jnr.unixsocket;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jnr.constants.platform.Errno;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import jnr.ffi.LastError;
import jnr.ffi.Runtime;
import jnr.unixsocket.BindHandler;
import jnr.unixsocket.Common;
import jnr.unixsocket.Native;
import jnr.unixsocket.SockAddrUnix;
import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketOptions;
import jnr.unixsocket.impl.AbstractNativeSocketChannel;

public class UnixSocketChannel
extends AbstractNativeSocketChannel {
    private State state;
    private UnixSocketAddress remoteAddress = null;
    private UnixSocketAddress localAddress = null;
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    private final BindHandler bindHandler;

    public static final UnixSocketChannel open() throws IOException {
        return new UnixSocketChannel();
    }

    public static final UnixSocketChannel open(UnixSocketAddress remote) throws IOException {
        UnixSocketChannel channel = new UnixSocketChannel();
        try {
            channel.connect(remote);
        }
        catch (IOException e) {
            channel.close();
            throw e;
        }
        return channel;
    }

    public static final UnixSocketChannel create() throws IOException {
        return new UnixSocketChannel();
    }

    public static final UnixSocketChannel[] pair() throws IOException {
        int[] sockets = new int[]{-1, -1};
        Native.socketpair(ProtocolFamily.PF_UNIX, Sock.SOCK_STREAM, 0, sockets);
        return new UnixSocketChannel[]{new UnixSocketChannel(sockets[0], State.CONNECTED, true), new UnixSocketChannel(sockets[1], State.CONNECTED, true)};
    }

    public static final UnixSocketChannel fromFD(int fd) {
        return new UnixSocketChannel(fd);
    }

    UnixSocketChannel() throws IOException {
        this(Native.socket(ProtocolFamily.PF_UNIX, Sock.SOCK_STREAM, 0));
    }

    UnixSocketChannel(int fd) {
        this(fd, State.CONNECTED, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    UnixSocketChannel(int fd, State initialState, boolean initialBoundState) {
        super(fd);
        this.stateLock.writeLock().lock();
        try {
            this.state = initialState;
            this.bindHandler = new BindHandler(initialBoundState);
        }
        finally {
            this.stateLock.writeLock().unlock();
        }
    }

    private boolean doConnect(SockAddrUnix remote) throws IOException {
        if (Native.connect(this.getFD(), remote, remote.length()) != 0) {
            Errno error = Errno.valueOf(LastError.getLastError(Runtime.getSystemRuntime()));
            switch (error) {
                case EAGAIN: 
                case EWOULDBLOCK: {
                    return false;
                }
            }
            throw new IOException(error.toString());
        }
        return true;
    }

    public boolean connect(UnixSocketAddress remote) throws IOException {
        this.remoteAddress = remote;
        if (!this.doConnect(this.remoteAddress.getStruct())) {
            this.stateLock.writeLock().lock();
            this.state = State.CONNECTING;
            this.stateLock.writeLock().unlock();
            return false;
        }
        this.stateLock.writeLock().lock();
        this.state = State.CONNECTED;
        this.stateLock.writeLock().unlock();
        return true;
    }

    boolean isBound() {
        return this.bindHandler.isBound();
    }

    @Override
    public boolean isConnected() {
        this.stateLock.readLock().lock();
        boolean result = this.state == State.CONNECTED;
        this.stateLock.readLock().unlock();
        return result;
    }

    private boolean isIdle() {
        this.stateLock.readLock().lock();
        boolean result = this.state == State.IDLE;
        this.stateLock.readLock().unlock();
        return result;
    }

    @Override
    public boolean isConnectionPending() {
        this.stateLock.readLock().lock();
        boolean isConnectionPending = this.state == State.CONNECTING;
        this.stateLock.readLock().unlock();
        return isConnectionPending;
    }

    @Override
    public boolean finishConnect() throws IOException {
        this.stateLock.writeLock().lock();
        try {
            switch (this.state) {
                case CONNECTED: {
                    boolean bl = true;
                    return bl;
                }
                case CONNECTING: {
                    if (!this.doConnect(this.remoteAddress.getStruct())) {
                        boolean bl = false;
                        return bl;
                    }
                    this.state = State.CONNECTED;
                    boolean bl = true;
                    return bl;
                }
            }
            throw new IllegalStateException("socket is not waiting for connect to complete");
        }
        finally {
            this.stateLock.writeLock().unlock();
        }
    }

    public final UnixSocketAddress getRemoteSocketAddress() {
        if (!this.isConnected()) {
            return null;
        }
        if (this.remoteAddress != null) {
            return this.remoteAddress;
        }
        this.remoteAddress = Common.getpeername(this.getFD());
        return this.remoteAddress;
    }

    public final UnixSocketAddress getLocalSocketAddress() {
        if (this.localAddress != null) {
            return this.localAddress;
        }
        this.localAddress = Common.getsockname(this.getFD());
        return this.localAddress;
    }

    @Override
    public boolean connect(SocketAddress remote) throws IOException {
        if (remote instanceof UnixSocketAddress) {
            return this.connect((UnixSocketAddress)remote);
        }
        throw new UnsupportedAddressTypeException();
    }

    @Override
    public UnixSocket socket() {
        return new UnixSocket(this);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (this.isConnected()) {
            return super.write(srcs, offset, length);
        }
        if (this.isIdle()) {
            return 0L;
        }
        throw new ClosedChannelException();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (this.isConnected()) {
            return super.read(dst);
        }
        if (this.isIdle()) {
            return 0;
        }
        throw new ClosedChannelException();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (this.isConnected()) {
            return super.write(src);
        }
        if (this.isIdle()) {
            return 0;
        }
        throw new ClosedChannelException();
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return this.remoteAddress;
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return this.localAddress;
    }

    @Override
    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        if (!this.supportedOptions().contains(name)) {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
        return Common.getSocketOption(this.getFD(), name);
    }

    @Override
    public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name may not be null");
        }
        if (!this.supportedOptions().contains(name)) {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
        Common.setSocketOption(this.getFD(), name, value);
        return this;
    }

    @Override
    public synchronized UnixSocketChannel bind(SocketAddress local) throws IOException {
        this.localAddress = this.bindHandler.bind(this.getFD(), local);
        return this;
    }

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultOptions = DefaultOptionsHolder.defaultOptions();

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<Object>> set = new HashSet<SocketOption<Object>>(5);
            set.add(UnixSocketOptions.SO_SNDBUF);
            set.add(UnixSocketOptions.SO_SNDTIMEO);
            set.add(UnixSocketOptions.SO_RCVBUF);
            set.add(UnixSocketOptions.SO_RCVTIMEO);
            set.add(UnixSocketOptions.SO_PEERCRED);
            set.add(UnixSocketOptions.SO_KEEPALIVE);
            set.add(UnixSocketOptions.SO_PASSCRED);
            return Collections.unmodifiableSet(set);
        }
    }

    static enum State {
        UNINITIALIZED,
        CONNECTED,
        IDLE,
        CONNECTING;

    }
}

