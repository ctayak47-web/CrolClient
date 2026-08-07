
package jnr.unixsocket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.UnsupportedAddressTypeException;
import jnr.unixsocket.Common;
import jnr.unixsocket.Native;
import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;

public class UnixServerSocket {
    final UnixServerSocketChannel channel;
    final int fd;
    volatile UnixSocketAddress localAddress;

    public UnixServerSocket() throws IOException {
        this.channel = new UnixServerSocketChannel(this);
        this.fd = this.channel.getFD();
    }

    UnixServerSocket(UnixServerSocketChannel channel) {
        this.channel = channel;
        this.fd = channel.getFD();
    }

    public UnixSocket accept() throws IOException {
        return new UnixSocket(this.channel.accept());
    }

    public void bind(SocketAddress endpoint) throws IOException {
        this.bind(endpoint, 128);
    }

    public void bind(SocketAddress endpoint, int backlog) throws IOException {
        if (null != endpoint && !(endpoint instanceof UnixSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }
        this.localAddress = Common.bind(this.fd, (UnixSocketAddress)endpoint);
        if (Native.listen(this.fd, backlog) < 0) {
            throw new IOException(Native.getLastErrorString());
        }
    }
}

