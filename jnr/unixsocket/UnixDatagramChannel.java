
package jnr.unixsocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import jnr.unixsocket.BindHandler;
import jnr.unixsocket.Common;
import jnr.unixsocket.Native;
import jnr.unixsocket.SockAddrUnix;
import jnr.unixsocket.UnixDatagramSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketOptions;
import jnr.unixsocket.impl.AbstractNativeDatagramChannel;

public class UnixDatagramChannel
extends AbstractNativeDatagramChannel {
    private State state;
    private UnixSocketAddress remoteAddress = null;
    private UnixSocketAddress localAddress = null;
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    private final BindHandler bindHandler;

    public static final UnixDatagramChannel open() throws IOException {
        return new UnixDatagramChannel();
    }

    public static final UnixDatagramChannel open(ProtocolFamily domain, int protocol) throws IOException {
        return new UnixDatagramChannel(domain, protocol);
    }

    public static final UnixDatagramChannel[] pair() throws IOException {
        int[] sockets = new int[]{-1, -1};
        Native.socketpair(ProtocolFamily.PF_UNIX, Sock.SOCK_DGRAM, 0, sockets);
        return new UnixDatagramChannel[]{new UnixDatagramChannel(sockets[0], State.CONNECTED, true), new UnixDatagramChannel(sockets[1], State.CONNECTED, true)};
    }

    private UnixDatagramChannel() throws IOException {
        this(Native.socket(ProtocolFamily.PF_UNIX, Sock.SOCK_DGRAM, 0));
    }

    UnixDatagramChannel(ProtocolFamily domain, int protocol) throws IOException {
        this(Native.socket(domain, Sock.SOCK_DGRAM, protocol));
    }

    UnixDatagramChannel(int fd) {
        this(fd, State.IDLE, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    UnixDatagramChannel(int fd, State initialState, boolean initialBoundState) {
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

    UnixDatagramChannel(int fd, UnixSocketAddress remote) throws IOException {
        this(fd);
        this.connect(remote);
    }

    @Override
    public UnixDatagramChannel bind(SocketAddress local) throws IOException {
        this.localAddress = this.bindHandler.bind(this.getFD(), local);
        return this;
    }

    public UnixDatagramChannel connect(UnixSocketAddress remote) {
        this.stateLock.writeLock().lock();
        this.remoteAddress = remote;
        this.state = State.CONNECTED;
        this.stateLock.writeLock().unlock();
        return this;
    }

    @Override
    public UnixDatagramChannel disconnect() throws IOException {
        this.stateLock.writeLock().lock();
        this.remoteAddress = null;
        this.state = State.IDLE;
        this.stateLock.writeLock().unlock();
        return this;
    }

    boolean isBound() {
        return this.bindHandler.isBound();
    }

    @Override
    public boolean isConnected() {
        this.stateLock.readLock().lock();
        boolean isConnected = this.state == State.CONNECTED;
        this.stateLock.readLock().unlock();
        return isConnected;
    }

    public final UnixSocketAddress getRemoteSocketAddress() {
        if (!this.isConnected()) {
            return null;
        }
        return this.remoteAddress != null ? this.remoteAddress : (this.remoteAddress = Common.getpeername(this.getFD()));
    }

    public final UnixSocketAddress getLocalSocketAddress() {
        return this.localAddress != null ? this.localAddress : (this.localAddress = Common.getsockname(this.getFD()));
    }

    @Override
    public UnixSocketAddress receive(ByteBuffer src) throws IOException {
        UnixSocketAddress remote = new UnixSocketAddress();
        int n = Native.recvfrom(this.getFD(), src, remote.getStruct());
        if (n < 0) {
            throw new IOException(Native.getLastErrorString());
        }
        return remote;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        UnixSocketAddress remote = null;
        if (null == target) {
            if (!this.isConnected()) throw new IllegalArgumentException("Destination address cannot be null on unconnected datagram sockets");
            remote = this.remoteAddress;
        } else {
            if (!(target instanceof UnixSocketAddress)) {
                throw new UnsupportedAddressTypeException();
            }
            remote = (UnixSocketAddress)target;
        }
        SockAddrUnix sa = null == remote ? null : remote.getStruct();
        int addrlen = null == sa ? 0 : sa.length();
        int n = Native.sendto(this.getFD(), src, sa, addrlen);
        if (n >= 0) return n;
        throw new IOException(Native.getLastErrorString());
    }

    @Override
    public DatagramChannel connect(SocketAddress remote) throws IOException {
        if (remote instanceof UnixSocketAddress) {
            return this.connect((UnixSocketAddress)remote);
        }
        throw new UnsupportedAddressTypeException();
    }

    @Override
    public UnixDatagramSocket socket() {
        try {
            return new UnixDatagramSocket(this);
        }
        catch (SocketException e) {
            throw new NullPointerException("Could not create UnixDatagramSocket");
        }
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (this.state == State.CONNECTED) {
            return super.write(srcs, offset, length);
        }
        if (this.state == State.IDLE) {
            return 0L;
        }
        throw new ClosedChannelException();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (this.state == State.CONNECTED) {
            return super.read(dst);
        }
        if (this.state == State.IDLE) {
            return 0;
        }
        throw new ClosedChannelException();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (this.state == State.CONNECTED) {
            return super.write(src);
        }
        if (this.state == State.IDLE) {
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
    public <T> DatagramChannel setOption(SocketOption<T> name, T value) throws IOException {
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
    public MembershipKey join(InetAddress group, NetworkInterface interf) {
        throw new UnsupportedOperationException("join is not supported");
    }

    @Override
    public MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source) {
        throw new UnsupportedOperationException("join is not supported");
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
            return Collections.unmodifiableSet(set);
        }
    }

    static enum State {
        UNINITIALIZED,
        CONNECTED,
        IDLE;

    }
}

