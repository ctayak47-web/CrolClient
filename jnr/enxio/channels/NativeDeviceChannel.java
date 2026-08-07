
package jnr.enxio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import jnr.enxio.channels.Native;
import jnr.enxio.channels.NativeFileSelectorProvider;
import jnr.enxio.channels.NativeSelectableChannel;
import jnr.enxio.channels.NativeSelectorProvider;

public class NativeDeviceChannel
extends AbstractSelectableChannel
implements ByteChannel,
NativeSelectableChannel {
    private final int fd;
    private final int validOps;
    private final boolean isFile;

    public NativeDeviceChannel(int fd) {
        this(fd, false);
    }

    public NativeDeviceChannel(int fd, boolean isFile) {
        this(NativeDeviceChannel.selectorProvider(isFile), fd, 5, isFile);
    }

    public NativeDeviceChannel(SelectorProvider provider, int fd, int ops, boolean isFile) {
        super(provider);
        this.fd = fd;
        this.validOps = ops;
        this.isFile = isFile;
    }

    private static SelectorProvider selectorProvider(boolean isFile) {
        return isFile ? NativeFileSelectorProvider.getInstance() : NativeSelectorProvider.getInstance();
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        int n = Native.close(this.fd);
        if (n < 0) {
            throw new IOException(Native.getLastErrorString());
        }
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

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int n = Native.read(this.fd, dst);
        switch (n) {
            case 0: {
                return -1;
            }
            case -1: {
                switch (Native.getLastError()) {
                    case EAGAIN: 
                    case EWOULDBLOCK: {
                        return 0;
                    }
                }
                throw new IOException(Native.getLastErrorString());
            }
        }
        return n;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int n = Native.write(this.fd, src);
        if (n < 0) {
            throw new IOException(Native.getLastErrorString());
        }
        return n;
    }
}

