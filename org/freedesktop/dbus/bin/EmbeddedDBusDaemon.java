
package org.freedesktop.dbus.bin;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.freedesktop.dbus.bin.DBusDaemon;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.config.TransportConfigBuilder;
import org.freedesktop.dbus.connections.transports.AbstractTransport;
import org.freedesktop.dbus.connections.transports.TransportBuilder;
import org.freedesktop.dbus.connections.transports.TransportConnection;
import org.freedesktop.dbus.exceptions.AuthenticationException;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.InvalidBusAddressException;
import org.freedesktop.dbus.exceptions.SocketClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedDBusDaemon
implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedDBusDaemon.class);
    private final BusAddress address;
    private DBusDaemon daemon;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private TransportBuilder.SaslAuthMode saslAuthMode;
    private String unixSocketFileOwner;
    private String unixSocketFileGroup;
    private PosixFilePermission[] unixSocketFilePermissions;
    private Consumer<AbstractTransport> connectCallback;
    private Consumer<AbstractTransport> bindCallback;
    private CountDownLatch startupLatch = new CountDownLatch(1);

    public EmbeddedDBusDaemon(BusAddress _address) {
        this.address = BusAddress.of(Objects.requireNonNull(_address, "Address required"));
    }

    public EmbeddedDBusDaemon(String _address) throws InvalidBusAddressException {
        this(BusAddress.of(_address));
    }

    @Override
    public synchronized void close() throws IOException {
        this.closed.set(true);
        this.startupLatch = new CountDownLatch(1);
        if (this.daemon != null) {
            this.daemon.close();
            try {
                this.daemon.join(5000L);
            }
            catch (InterruptedException _ex) {
                LOGGER.debug("Interrupted while waiting for daemon thread to terminate");
                Thread.currentThread().interrupt();
            }
            this.daemon = null;
        }
    }

    public void startInForeground() {
        block2: {
            try {
                this.closed.set(false);
                this.startListening();
            }
            catch (IOException | DBusException _ex) {
                if (this.closed.get()) break block2;
                throw new RuntimeException(_ex);
            }
        }
    }

    public void startInBackground() {
        Thread thread2 = new Thread(this::startInForeground);
        String threadName = this.address.toString().replaceAll("^([^,]+),.+", "$1");
        thread2.setName("EmbeddedDBusDaemon-" + threadName);
        thread2.setDaemon(true);
        thread2.setUncaughtExceptionHandler((th, ex) -> LOGGER.error("Got uncaught exception", ex));
        thread2.start();
    }

    public void startInBackgroundAndWait(long _maxWaitMillis) throws IllegalStateException {
        this.startInBackground();
        try {
            if (!this.startupLatch.await(_maxWaitMillis, TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("Daemon not started after " + _maxWaitMillis + " milliseconds");
            }
        }
        catch (InterruptedException _ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Startup of daemon interrupted");
        }
    }

    public void startInBackgroundAndWait() throws IllegalStateException {
        this.startInBackground();
        try {
            this.startupLatch.await();
        }
        catch (InterruptedException _ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for daemon to start");
        }
    }

    public synchronized boolean isRunning() {
        return this.startupLatch.getCount() == 0L && this.daemon != null && this.daemon.isRunning();
    }

    public TransportBuilder.SaslAuthMode getSaslAuthMode() {
        return this.saslAuthMode;
    }

    public void setSaslAuthMode(TransportBuilder.SaslAuthMode _saslAuthMode) {
        this.saslAuthMode = _saslAuthMode;
    }

    public void setUnixSocketOwner(String _owner) {
        this.unixSocketFileOwner = _owner;
    }

    public void setUnixSocketGroup(String _group) {
        this.unixSocketFileGroup = _group;
    }

    public void setUnixSocketPermissions(PosixFilePermission ... _permissions) {
        this.unixSocketFilePermissions = _permissions;
    }

    public Consumer<AbstractTransport> getConnectCallback() {
        return this.connectCallback;
    }

    public void setConnectCallback(Consumer<AbstractTransport> _connectCallback) {
        this.connectCallback = _connectCallback;
    }

    public Consumer<AbstractTransport> getBindCallback() {
        return this.bindCallback;
    }

    public void setBindCallback(Consumer<AbstractTransport> _callback) {
        this.bindCallback = _callback;
    }

    private synchronized void setDaemonAndStart(AbstractTransport _transport) {
        this.daemon = new DBusDaemon(_transport);
        this.daemon.start();
    }

    private void startListening() throws IOException, DBusException {
        if (!TransportBuilder.getRegisteredBusTypes().contains(this.address.getBusType())) {
            throw new IllegalArgumentException("Unknown or unsupported address type: " + this.address.getType());
        }
        LOGGER.debug("About to initialize transport on: {}", (Object)this.address);
        try (AbstractTransport transport = ((TransportBuilder)((TransportConfigBuilder)((TransportConfigBuilder)((TransportConfigBuilder)((TransportConfigBuilder)((TransportConfigBuilder)TransportBuilder.create(this.address).configure().withUnixSocketFileOwner(this.unixSocketFileOwner).withUnixSocketFileGroup(this.unixSocketFileGroup)).withUnixSocketFilePermissions(this.unixSocketFilePermissions)).withPreConnectCallback(this.connectCallback)).withAfterBindCallback(x -> {
            if (this.bindCallback != null) {
                this.bindCallback.accept((AbstractTransport)x);
            }
            this.startupLatch.countDown();
        })).withAutoConnect(false)).configureSasl().withAuthMode(this.getSaslAuthMode()).back().back()).build();){
            this.setDaemonAndStart(transport);
            do {
                try {
                    LOGGER.debug("Begin listening to: {}", (Object)transport);
                    TransportConnection s = transport.listen();
                    this.daemon.addSock(s);
                }
                catch (AuthenticationException _ex) {
                    LOGGER.error("Authentication failed", _ex);
                }
                catch (SocketClosedException _ex) {
                    LOGGER.debug("Connection closed", _ex);
                }
            } while (this.daemon.isRunning());
        }
    }
}

