
package jnr.constants.platform.openbsd;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum AddressFamily implements Constant
{
    AF_UNSPEC(0L),
    AF_LOCAL(1L),
    AF_UNIX(1L),
    AF_INET(2L),
    AF_IMPLINK(3L),
    AF_PUP(4L),
    AF_CHAOS(5L),
    AF_NS(6L),
    AF_ISO(7L),
    AF_OSI(7L),
    AF_ECMA(8L),
    AF_DATAKIT(9L),
    AF_CCITT(10L),
    AF_SNA(11L),
    AF_DECnet(12L),
    AF_DLI(13L),
    AF_LAT(14L),
    AF_HYLINK(15L),
    AF_APPLETALK(16L),
    AF_ROUTE(17L),
    AF_LINK(18L),
    pseudo_AF_XTP(19L),
    AF_COIP(20L),
    AF_CNT(21L),
    pseudo_AF_RTIP(22L),
    AF_IPX(23L),
    AF_SIP(29L),
    pseudo_AF_PIP(25L),
    AF_ISDN(26L),
    AF_E164(26L),
    AF_INET6(24L),
    AF_NATM(27L),
    pseudo_AF_HDRCMPLT(31L),
    AF_MAX(36L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 36L;

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
            map.put(AF_IMPLINK, "AF_IMPLINK");
            map.put(AF_PUP, "AF_PUP");
            map.put(AF_CHAOS, "AF_CHAOS");
            map.put(AF_NS, "AF_NS");
            map.put(AF_ISO, "AF_ISO");
            map.put(AF_OSI, "AF_OSI");
            map.put(AF_ECMA, "AF_ECMA");
            map.put(AF_DATAKIT, "AF_DATAKIT");
            map.put(AF_CCITT, "AF_CCITT");
            map.put(AF_SNA, "AF_SNA");
            map.put(AF_DECnet, "AF_DECnet");
            map.put(AF_DLI, "AF_DLI");
            map.put(AF_LAT, "AF_LAT");
            map.put(AF_HYLINK, "AF_HYLINK");
            map.put(AF_APPLETALK, "AF_APPLETALK");
            map.put(AF_ROUTE, "AF_ROUTE");
            map.put(AF_LINK, "AF_LINK");
            map.put(pseudo_AF_XTP, "pseudo_AF_XTP");
            map.put(AF_COIP, "AF_COIP");
            map.put(AF_CNT, "AF_CNT");
            map.put(pseudo_AF_RTIP, "pseudo_AF_RTIP");
            map.put(AF_IPX, "AF_IPX");
            map.put(AF_SIP, "AF_SIP");
            map.put(pseudo_AF_PIP, "pseudo_AF_PIP");
            map.put(AF_ISDN, "AF_ISDN");
            map.put(AF_E164, "AF_E164");
            map.put(AF_INET6, "AF_INET6");
            map.put(AF_NATM, "AF_NATM");
            map.put(pseudo_AF_HDRCMPLT, "pseudo_AF_HDRCMPLT");
            map.put(AF_MAX, "AF_MAX");
            return map;
        }
    }
}

