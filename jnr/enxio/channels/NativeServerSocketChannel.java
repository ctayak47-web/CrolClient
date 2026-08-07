
package jnr.enxio.channels;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import jnr.enxio.channels.Native;
import jnr.enxio.channels.NativeSelectableChannel;
import jnr.enxio.channels.NativeSelectorProvider;

public class NativeServerSocketChannel
extends AbstractSelectableChannel
implements NativeSelectableChannel {
    private final int fd;
    private final int validOps;

    public NativeServerSocketChannel(int fd) {
        this(NativeSelectorProvider.getInstance(), fd, 17);
    }

    public NativeServerSocketChannel(SelectorProvider provider, int fd, int ops) {
        super(provider);
        this.fd = fd;
        this.validOps = ops;
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        Native.close(this.fd);
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        Native.setBlocking(this.fd, block);
    }

    @Override
    public final int validOps() {
        return this.validOps;
    }

    @Override
    public final int getFD() {
        return this.fd;
    }
}

