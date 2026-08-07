
package jnr.constants.platform.linux;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum SocketLevel implements Constant
{
    SOL_SOCKET(1L),
    SOL_IP(0L),
    SOL_TCP(6L),
    SOL_UDP(17L),
    SOL_IPV6(41L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 41L;

    private SocketLevel(long value) {
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
        public static final Map<SocketLevel, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<SocketLevel, String> generateTable() {
            EnumMap<SocketLevel, String> map = new EnumMap<SocketLevel, String>(SocketLevel.class);
            map.put(SOL_SOCKET, "SOL_SOCKET");
            map.put(SOL_IP, "SOL_IP");
            map.put(SOL_TCP, "SOL_TCP");
            map.put(SOL_UDP, "SOL_UDP");
            map.put(SOL_IPV6, "SOL_IPV6");
            return map;
        }
    }
}

