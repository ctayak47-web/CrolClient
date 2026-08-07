
package jnr.unixsocket;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.spi.SelectorProvider;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import jnr.ffi.byref.IntByReference;
import jnr.unixsocket.Native;
import jnr.unixsocket.SockAddrUnix;
import jnr.unixsocket.UnixServerSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import jnr.unixsocket.impl.AbstractNativeServerSocketChannel;

public class UnixServerSocketChannel
extends AbstractNativeServerSocketChannel {
    private final UnixServerSocket socket = new UnixServerSocket(this);

    UnixServerSocketChannel(UnixServerSocket socket) throws IOException {
        super(Native.socket(ProtocolFamily.PF_UNIX, Sock.SOCK_STREAM, 0));
    }

    UnixServerSocketChannel(SelectorProvider provider, int fd) {
        super(provider, fd, 17);
    }

    public static UnixServerSocketChannel open() throws IOException {
        return new UnixServerSocket().channel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public UnixSocketChannel accept() throws IOException {
        UnixSocketAddress remote = new UnixSocketAddress();
        SockAddrUnix addr = remote.getStruct();
        int maxLength = addr.getMaximumLength();
        IntByReference len = new IntByReference(maxLength);
        int clientfd = -1;
        this.begin();
        try {
            clientfd = Native.accept(this.getFD(), addr, len);
            this.end(clientfd >= 0);
        }
        catch (Throwable throwable) {
            this.end(clientfd >= 0);
            throw throwable;
        }
        if (clientfd < 0) {
            if (this.isBlocking()) {
                switch (Native.getLastError()) {
                    case EBADF: {
                        throw new ClosedChannelException();
                    }
                    case EINVAL: {
                        throw new NotYetBoundException();
                    }
                }
                throw new IOException("accept failed: " + Native.getLastErrorString());
            }
            return null;
        }
        addr.updatePath((Integer)len.getValue());
        Native.setBlocking(clientfd, true);
        return new UnixSocketChannel(clientfd);
    }

    public final UnixServerSocket socket() {
        return this.socket;
    }

    public final UnixSocketAddress getRemoteSocketAddress() {
        return null;
    }

    public final UnixSocketAddress getLocalSocketAddress() {
        return this.socket.localAddress;
    }
}

