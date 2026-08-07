
package jnr.enxio.channels;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import jnr.enxio.channels.PollSelector;

public final class NativeFileSelectorProvider
extends SelectorProvider {
    public static final SelectorProvider getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public DatagramChannel openDatagramChannel() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Pipe openPipe() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractSelector openSelector() throws IOException {
        return new PollSelector(this);
    }

    @Override
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SocketChannel openSocketChannel() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static final class SingletonHolder {
        static NativeFileSelectorProvider INSTANCE = new NativeFileSelectorProvider();

        private SingletonHolder() {
        }
    }
}

