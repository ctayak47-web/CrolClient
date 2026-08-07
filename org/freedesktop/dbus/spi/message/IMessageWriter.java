
package org.freedesktop.dbus.spi.message;

import java.io.Closeable;
import java.io.IOException;
import org.freedesktop.dbus.messages.Message;

public interface IMessageWriter
extends Closeable {
    public void writeMessage(Message var1) throws IOException;

    public boolean isClosed();
}

