
package jnr.unixsocket.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import jnr.constants.platform.Errno;
import jnr.constants.platform.Shutdown;
import jnr.enxio.channels.Native;
import jnr.enxio.channels.NativeException;
import jnr.enxio.channels.NativeSelectableChannel;
import jnr.enxio.channels.NativeSelectorProvider;
import jnr.unixsocket.impl.Common;

public abstract class AbstractNativeSocketChannel
extends SocketChannel
implements ByteChannel,
NativeSelectableChannel {
    private final Common common;
    private static final int SHUT_RD = Shutdown.SHUT_RD.intValue();
    private static final int SHUT_WR = Shutdown.SHUT_WR.intValue();

    public AbstractNativeSocketChannel(int fd) {
        this(NativeSelectorProvider.getInstance(), fd);
    }

    AbstractNativeSocketChannel(SelectorProvider provider, int fd) {
        super(provider);
        this.common = new Common(fd);
    }

    public void setFD(int fd) {
        this.common.setFD(fd);
    }

    @Override
    public final int getFD() {
        return this.common.getFD();
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        if (this.isConnected()) {
            this.shutdownInput();
            this.shutdownOutput();
        }
        Native.close(this.common.getFD());
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        Native.setBlocking(this.common.getFD(), block);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return this.common.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return this.common.read(dsts, offset, length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return this.common.write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return this.common.write(srcs, offset, length);
    }

    @Override
    public SocketChannel shutdownInput() throws IOException {
        int n = Native.shutdown(this.common.getFD(), SHUT_RD);
        if (n < 0 && Native.getLastError() != Errno.ENOTCONN) {
            throw new NativeException(Native.getLastErrorString(), Native.getLastError());
        }
        return this;
    }

    @Override
    public SocketChannel shutdownOutput() throws IOException {
        int n = Native.shutdown(this.common.getFD(), SHUT_WR);
        if (n < 0 && Native.getLastError() != Errno.ENOTCONN) {
            throw new NativeException(Native.getLastErrorString(), Native.getLastError());
        }
        return this;
    }
}

