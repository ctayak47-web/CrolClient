
package jnr.constants.platform.windows;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum ProtocolFamily implements Constant
{
    PF_UNSPEC(0L),
    PF_UNIX(1L),
    PF_INET(2L),
    PF_IMPLINK(3L),
    PF_PUP(4L),
    PF_CHAOS(5L),
    PF_NS(6L),
    PF_ISO(7L),
    PF_OSI(7L),
    PF_ECMA(8L),
    PF_DATAKIT(9L),
    PF_CCITT(10L),
    PF_SNA(11L),
    PF_DECnet(12L),
    PF_DLI(13L),
    PF_LAT(14L),
    PF_HYLINK(15L),
    PF_APPLETALK(16L),
    PF_IPX(6L),
    PF_INET6(23L),
    PF_ATM(22L),
    PF_MAX(33L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 33L;

    private ProtocolFamily(long value) {
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
        public static final Map<ProtocolFamily, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<ProtocolFamily, String> generateTable() {
            EnumMap<ProtocolFamily, String> map = new EnumMap<ProtocolFamily, String>(ProtocolFamily.class);
            map.put(PF_UNSPEC, "PF_UNSPEC");
            map.put(PF_UNIX, "PF_UNIX");
            map.put(PF_INET, "PF_INET");
            map.put(PF_IMPLINK, "PF_IMPLINK");
            map.put(PF_PUP, "PF_PUP");
            map.put(PF_CHAOS, "PF_CHAOS");
            map.put(PF_NS, "PF_NS");
            map.put(PF_ISO, "PF_ISO");
            map.put(PF_OSI, "PF_OSI");
            map.put(PF_ECMA, "PF_ECMA");
            map.put(PF_DATAKIT, "PF_DATAKIT");
            map.put(PF_CCITT, "PF_CCITT");
            map.put(PF_SNA, "PF_SNA");
            map.put(PF_DECnet, "PF_DECnet");
            map.put(PF_DLI, "PF_DLI");
            map.put(PF_LAT, "PF_LAT");
            map.put(PF_HYLINK, "PF_HYLINK");
            map.put(PF_APPLETALK, "PF_APPLETALK");
            map.put(PF_IPX, "PF_IPX");
            map.put(PF_INET6, "PF_INET6");
            map.put(PF_ATM, "PF_ATM");
            map.put(PF_MAX, "PF_MAX");
            return map;
        }
    }
}

