
package jnr.unixsocket.impl;

import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import jnr.constants.platform.Shutdown;
import jnr.enxio.channels.Native;
import jnr.enxio.channels.NativeServerSocketChannel;

public abstract class AbstractNativeServerSocketChannel
extends NativeServerSocketChannel {
    private static final int SHUT_RD = Shutdown.SHUT_RD.intValue();

    public AbstractNativeServerSocketChannel(int fd) {
        super(fd);
    }

    public AbstractNativeServerSocketChannel(SelectorProvider provider, int fd, int ops) {
        super(provider, fd, ops);
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        Native.shutdown(this.getFD(), SHUT_RD);
        Native.close(this.getFD());
    }
}

