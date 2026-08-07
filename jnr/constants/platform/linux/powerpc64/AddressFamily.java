
package jnr.constants.platform.linux.powerpc64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum AddressFamily implements Constant
{
    AF_UNSPEC(0L),
    AF_LOCAL(1L),
    AF_UNIX(1L),
    AF_INET(2L),
    AF_SNA(22L),
    AF_DECnet(12L),
    AF_APPLETALK(5L),
    AF_ROUTE(16L),
    AF_IPX(4L),
    AF_ISDN(34L),
    AF_INET6(10L),
    AF_AX25(3L),
    AF_MAX(44L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 44L;

    private AddressFamily(long value) {
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
        public static final Map<AddressFamily, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<AddressFamily, String> generateTable() {
            EnumMap<AddressFamily, String> map = new EnumMap<AddressFamily, String>(AddressFamily.class);
            map.put(AF_UNSPEC, "AF_UNSPEC");
            map.put(AF_LOCAL, "AF_LOCAL");
            map.put(AF_UNIX, "AF_UNIX");
            map.put(AF_INET, "AF_INET");
            map.put(AF_SNA, "AF_SNA");
            map.put(AF_DECnet, "AF_DECnet");
            map.put(AF_APPLETALK, "AF_APPLETALK");
            map.put(AF_ROUTE, "AF_ROUTE");
            map.put(AF_IPX, "AF_IPX");
            map.put(AF_ISDN, "AF_ISDN");
            map.put(AF_INET6, "AF_INET6");
            map.put(AF_AX25, "AF_AX25");
            map.put(AF_MAX, "AF_MAX");
            return map;
        }
    }
}

