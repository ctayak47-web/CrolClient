
package org.freedesktop.dbus.transport.jnr;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import jnr.unixsocket.Credentials;
import jnr.unixsocket.UnixSocketOptions;

public final class JnrUnixSocketHelper {
    private JnrUnixSocketHelper() {
    }

    public static int getUid(SocketChannel _sock) throws IOException {
        if (_sock == null) {
            return -1;
        }
        Credentials credentials = _sock.getOption(UnixSocketOptions.SO_PEERCRED);
        return credentials.getUid();
    }
}

