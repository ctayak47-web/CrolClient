
package jnr.constants.platform.solaris;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum TCP implements Constant
{
    TCP_MSS(536L),
    TCP_NODELAY(1L),
    TCP_MAXSEG(2L),
    TCP_KEEPALIVE(8L),
    TCP_CORK(24L),
    TCP_INFO(34L),
    TCP_KEEPCNT(31L),
    TCP_KEEPIDLE(29L),
    TCP_KEEPINTVL(30L),
    TCP_LINGER2(28L),
    TCP_MD5SIG(36L),
    TCP_CONGESTION(35L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 536L;

    private TCP(long value) {
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
        public static final Map<TCP, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<TCP, String> generateTable() {
            EnumMap<TCP, String> map = new EnumMap<TCP, String>(TCP.class);
            map.put(TCP_MSS, "TCP_MSS");
            map.put(TCP_NODELAY, "TCP_NODELAY");
            map.put(TCP_MAXSEG, "TCP_MAXSEG");
            map.put(TCP_KEEPALIVE, "TCP_KEEPALIVE");
            map.put(TCP_CORK, "TCP_CORK");
            map.put(TCP_INFO, "TCP_INFO");
            map.put(TCP_KEEPCNT, "TCP_KEEPCNT");
            map.put(TCP_KEEPIDLE, "TCP_KEEPIDLE");
            map.put(TCP_KEEPINTVL, "TCP_KEEPINTVL");
            map.put(TCP_LINGER2, "TCP_LINGER2");
            map.put(TCP_MD5SIG, "TCP_MD5SIG");
            map.put(TCP_CONGESTION, "TCP_CONGESTION");
            return map;
        }
    }
}

