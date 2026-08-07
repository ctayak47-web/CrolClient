
package jnr.constants.platform.freebsd.aarch64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum NameInfo implements Constant
{
    NI_MAXHOST(1025L),
    NI_MAXSERV(32L),
    NI_NOFQDN(1L),
    NI_NUMERICHOST(2L),
    NI_NAMEREQD(4L),
    NI_NUMERICSERV(8L),
    NI_DGRAM(16L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 1025L;

    private NameInfo(long value) {
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
        public static final Map<NameInfo, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<NameInfo, String> generateTable() {
            EnumMap<NameInfo, String> map = new EnumMap<NameInfo, String>(NameInfo.class);
            map.put(NI_MAXHOST, "NI_MAXHOST");
            map.put(NI_MAXSERV, "NI_MAXSERV");
            map.put(NI_NOFQDN, "NI_NOFQDN");
            map.put(NI_NUMERICHOST, "NI_NUMERICHOST");
            map.put(NI_NAMEREQD, "NI_NAMEREQD");
            map.put(NI_NUMERICSERV, "NI_NUMERICSERV");
            map.put(NI_DGRAM, "NI_DGRAM");
            return map;
        }
    }
}

