
package jnr.unixsocket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.concurrent.atomic.AtomicBoolean;
import jnr.unixsocket.Common;
import jnr.unixsocket.UnixSocketAddress;

final class BindHandler {
    private final AtomicBoolean bound;

    BindHandler(boolean initialState) {
        this.bound = new AtomicBoolean(initialState);
    }

    boolean isBound() {
        return this.bound.get();
    }

    synchronized UnixSocketAddress bind(int fd, SocketAddress local) throws IOException {
        if (null != local && !(local instanceof UnixSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }
        if (this.bound.get()) {
            throw new AlreadyBoundException();
        }
        UnixSocketAddress ret = Common.bind(fd, (UnixSocketAddress)local);
        this.bound.set(true);
        return ret;
    }
}

