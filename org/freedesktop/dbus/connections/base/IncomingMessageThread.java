
package org.freedesktop.dbus.connections.base;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.base.ConnectionMessageHandler;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.IllegalThreadPoolStateException;
import org.freedesktop.dbus.interfaces.FatalException;
import org.freedesktop.dbus.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncomingMessageThread
extends Thread {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private volatile boolean terminate;
    private final ConnectionMessageHandler connection;

    public IncomingMessageThread(ConnectionMessageHandler _connection, BusAddress _busAddress) {
        this.connection = Objects.requireNonNull(_connection);
        this.setName("DBusConnection [listener=" + _busAddress.isListeningSocket() + "]");
        this.setDaemon(true);
    }

    public void terminate() {
        this.terminate = true;
        this.interrupt();
    }

    @Override
    public void run() {
        while (!this.terminate) {
            Message msg = null;
            try {
                msg = this.connection.readIncoming();
                if (msg == null) continue;
                this.logger.trace("Read message from {}: {}", (Object)this.connection.getTransport(), (Object)msg);
                this.connection.handleMessage(msg);
            }
            catch (RejectedExecutionException | DBusException | IllegalThreadPoolStateException _ex) {
                if (_ex instanceof FatalException) {
                    if (this.terminate) {
                        return;
                    }
                    this.logger.error("FatalException in connection thread", _ex);
                    if (this.connection.isConnected()) {
                        this.terminate = true;
                        Throwable throwable = _ex.getCause();
                        if (throwable instanceof IOException) {
                            IOException ioe = (IOException)throwable;
                            this.connection.internalDisconnect(ioe);
                        } else {
                            this.connection.internalDisconnect(null);
                        }
                    }
                    return;
                }
                if (this.terminate) continue;
                this.logger.error("Exception in connection thread", _ex);
            }
        }
    }
}

