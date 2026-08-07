
package jnr.constants.platform.openbsd;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum SocketOption implements Constant
{
    SO_DEBUG(1L),
    SO_ACCEPTCONN(2L),
    SO_REUSEADDR(4L),
    SO_KEEPALIVE(8L),
    SO_DONTROUTE(16L),
    SO_BROADCAST(32L),
    SO_USELOOPBACK(64L),
    SO_LINGER(128L),
    SO_OOBINLINE(256L),
    SO_REUSEPORT(512L),
    SO_TIMESTAMP(2048L),
    SO_SNDBUF(4097L),
    SO_RCVBUF(4098L),
    SO_SNDLOWAT(4099L),
    SO_RCVLOWAT(4100L),
    SO_SNDTIMEO(4101L),
    SO_RCVTIMEO(4102L),
    SO_ERROR(4103L),
    SO_TYPE(4104L),
    SO_PEERCRED(4130L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 4130L;

    private SocketOption(long value) {
        this.value = value;
    }

    public final String toString() {
        return StringTable.descriptions.get(this);
    }

    public final int value() {
        return (int)this.value;
    }

    @Override
    public final int intValue() {
        return (int)this.value;
    }

    @Override
    public final long longValue() {
        return this.value;
    }

    @Override
    public final boolean defined() {
        return true;
    }

    static final class StringTable {
        public static final Map<SocketOption, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<SocketOption, String> generateTable() {
            EnumMap<SocketOption, String> map = new EnumMap<SocketOption, String>(SocketOption.class);
            map.put(SO_DEBUG, "SO_DEBUG");
            map.put(SO_ACCEPTCONN, "SO_ACCEPTCONN");
            map.put(SO_REUSEADDR, "SO_REUSEADDR");
            map.put(SO_KEEPALIVE, "SO_KEEPALIVE");
            map.put(SO_DONTROUTE, "SO_DONTROUTE");
            map.put(SO_BROADCAST, "SO_BROADCAST");
            map.put(SO_USELOOPBACK, "SO_USELOOPBACK");
            map.put(SO_LINGER, "SO_LINGER");
            map.put(SO_OOBINLINE, "SO_OOBINLINE");
            map.put(SO_REUSEPORT, "SO_REUSEPORT");
            map.put(SO_TIMESTAMP, "SO_TIMESTAMP");
            map.put(SO_SNDBUF, "SO_SNDBUF");
            map.put(SO_RCVBUF, "SO_RCVBUF");
            map.put(SO_SNDLOWAT, "SO_SNDLOWAT");
            map.put(SO_RCVLOWAT, "SO_RCVLOWAT");
            map.put(SO_SNDTIMEO, "SO_SNDTIMEO");
            map.put(SO_RCVTIMEO, "SO_RCVTIMEO");
            map.put(SO_ERROR, "SO_ERROR");
            map.put(SO_TYPE, "SO_TYPE");
            map.put(SO_PEERCRED, "SO_PEERCRED");
            return map;
        }
    }
}

