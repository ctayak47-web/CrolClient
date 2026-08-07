
package jnr.constants.platform.freebsd;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum TCP implements Constant
{
    TCP_MAX_SACK(4L),
    TCP_MSS(536L),
    TCP_MINMSS(216L),
    TCP_MAXWIN(65535L),
    TCP_MAX_WINSHIFT(14L),
    TCP_MAXBURST(4L),
    TCP_MAXHLEN(60L),
    TCP_MAXOLEN(40L),
    TCP_NODELAY(1L),
    TCP_MAXSEG(2L),
    TCP_NOPUSH(4L),
    TCP_NOOPT(8L),
    TCP_INFO(32L),
    TCP_KEEPCNT(1024L),
    TCP_KEEPIDLE(256L),
    TCP_KEEPINTVL(512L),
    TCP_MD5SIG(16L),
    TCP_FASTOPEN(1025L),
    TCP_CONGESTION(64L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 65535L;

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
            map.put(TCP_MAX_SACK, "TCP_MAX_SACK");
            map.put(TCP_MSS, "TCP_MSS");
            map.put(TCP_MINMSS, "TCP_MINMSS");
            map.put(TCP_MAXWIN, "TCP_MAXWIN");
            map.put(TCP_MAX_WINSHIFT, "TCP_MAX_WINSHIFT");
            map.put(TCP_MAXBURST, "TCP_MAXBURST");
            map.put(TCP_MAXHLEN, "TCP_MAXHLEN");
            map.put(TCP_MAXOLEN, "TCP_MAXOLEN");
            map.put(TCP_NODELAY, "TCP_NODELAY");
            map.put(TCP_MAXSEG, "TCP_MAXSEG");
            map.put(TCP_NOPUSH, "TCP_NOPUSH");
            map.put(TCP_NOOPT, "TCP_NOOPT");
            map.put(TCP_INFO, "TCP_INFO");
            map.put(TCP_KEEPCNT, "TCP_KEEPCNT");
            map.put(TCP_KEEPIDLE, "TCP_KEEPIDLE");
            map.put(TCP_KEEPINTVL, "TCP_KEEPINTVL");
            map.put(TCP_MD5SIG, "TCP_MD5SIG");
            map.put(TCP_FASTOPEN, "TCP_FASTOPEN");
            map.put(TCP_CONGESTION, "TCP_CONGESTION");
            return map;
        }
    }
}

