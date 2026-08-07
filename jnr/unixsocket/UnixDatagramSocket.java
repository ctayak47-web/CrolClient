
package jnr.unixsocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import jnr.unixsocket.Credentials;
import jnr.unixsocket.UnixDatagramChannel;
import jnr.unixsocket.UnixSocketOptions;

public class UnixDatagramSocket
extends DatagramSocket {
    private final UnixDatagramChannel chan;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    UnixDatagramSocket(UnixDatagramChannel channel) throws SocketException {
        this.chan = channel;
    }

    public UnixDatagramSocket() throws SocketException {
        this.chan = null;
    }

    @Override
    public void bind(SocketAddress local) throws SocketException {
        if (null != this.chan) {
            if (this.isClosed()) {
                throw new SocketException("Socket is closed");
            }
            if (this.isBound()) {
                throw new SocketException("already bound");
            }
            try {
                this.chan.bind(local);
            }
            catch (IOException e) {
                throw (SocketException)new SocketException().initCause(e);
            }
        }
    }

    @Override
    public synchronized void disconnect() {
        if (this.isClosed()) {
            return;
        }
        if (null != this.chan) {
            try {
                this.chan.disconnect();
            }
            catch (IOException e) {
                this.ignore();
            }
        }
    }

    @Override
    public synchronized void close() {
        if (null != this.chan && this.closed.compareAndSet(false, true)) {
            try {
                this.chan.close();
            }
            catch (IOException e) {
                this.ignore();
            }
        }
    }

    @Override
    public void connect(SocketAddress addr) throws SocketException {
        try {
            this.chan.connect(addr);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void connect(InetAddress addr, int port) {
        throw new UnsupportedOperationException("connect(InetAddress, int) is not supported");
    }

    @Override
    public DatagramChannel getChannel() {
        return this.chan;
    }

    @Override
    public InetAddress getInetAddress() {
        return null;
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        if (this.isClosed()) {
            return null;
        }
        if (null == this.chan) {
            return null;
        }
        return this.chan.getLocalSocketAddress();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        if (!this.isConnected()) {
            return null;
        }
        return this.chan.getRemoteSocketAddress();
    }

    @Override
    public boolean isBound() {
        if (null == this.chan) {
            return false;
        }
        return this.chan.isBound();
    }

    @Override
    public boolean isClosed() {
        if (null == this.chan) {
            return false;
        }
        return this.closed.get();
    }

    @Override
    public boolean isConnected() {
        if (null == this.chan) {
            return false;
        }
        return this.chan.isConnected();
    }

    public final Credentials getCredentials() throws SocketException {
        if (!this.chan.isConnected()) {
            return null;
        }
        try {
            return this.chan.getOption(UnixSocketOptions.SO_PEERCRED);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        try {
            return this.chan.getOption(UnixSocketOptions.SO_RCVBUF);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        try {
            return this.chan.getOption(UnixSocketOptions.SO_SNDBUF);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public int getSoTimeout() throws SocketException {
        try {
            return this.chan.getOption(UnixSocketOptions.SO_RCVTIMEO);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        try {
            this.chan.setOption((SocketOption)UnixSocketOptions.SO_RCVBUF, (Object)size);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        try {
            this.chan.setOption((SocketOption)UnixSocketOptions.SO_SNDBUF, (Object)size);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        try {
            this.chan.setOption((SocketOption)UnixSocketOptions.SO_RCVTIMEO, (Object)timeout);
        }
        catch (IOException e) {
            throw (SocketException)new SocketException().initCause(e);
        }
    }

    @Override
    public void send(DatagramPacket p) throws IOException {
        throw new UnsupportedOperationException("sending DatagramPackets is not supported");
    }

    @Override
    public synchronized void receive(DatagramPacket p) throws IOException {
        throw new UnsupportedOperationException("receiving DatagramPackets is not supported");
    }

    private void ignore() {
    }
}

