
package jnr.unixsocket;

import java.net.SocketOption;
import jnr.unixsocket.Credentials;

public final class UnixSocketOptions {
    public static final SocketOption<Integer> SO_SNDBUF = new GenericOption<Integer>("SO_SNDBUF", Integer.class);
    public static final SocketOption<Integer> SO_SNDTIMEO = new GenericOption<Integer>("SO_SNDTIMEO", Integer.class);
    public static final SocketOption<Integer> SO_RCVBUF = new GenericOption<Integer>("SO_RCVBUF", Integer.class);
    public static final SocketOption<Integer> SO_RCVTIMEO = new GenericOption<Integer>("SO_RCVTIMEO", Integer.class);
    public static final SocketOption<Boolean> SO_KEEPALIVE = new GenericOption<Boolean>("SO_KEEPALIVE", Boolean.class);
    public static final SocketOption<Credentials> SO_PEERCRED = new GenericOption<Credentials>("SO_PEERCRED", Credentials.class);
    public static final SocketOption<Boolean> SO_PASSCRED = new GenericOption<Boolean>("SO_PASSCRED", Boolean.class);

    private static class GenericOption<T>
    implements SocketOption<T> {
        private final String name;
        private final Class<T> type;

        GenericOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Class<T> type() {
            return this.type;
        }

        public String toString() {
            return this.name;
        }
    }
}

