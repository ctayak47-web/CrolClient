
package jnr.constants.platform.linux;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum ProtocolFamily implements Constant
{
    PF_UNSPEC(0L),
    PF_LOCAL(1L),
    PF_UNIX(1L),
    PF_INET(2L),
    PF_SNA(22L),
    PF_DECnet(12L),
    PF_APPLETALK(5L),
    PF_ROUTE(16L),
    PF_IPX(4L),
    PF_ISDN(34L),
    PF_KEY(15L),
    PF_INET6(10L),
    PF_NETLINK(16L),
    PF_RDS(21L),
    PF_PPPOX(24L),
    PF_LLC(26L),
    PF_IB(27L),
    PF_MPLS(28L),
    PF_CAN(29L),
    PF_TIPC(30L),
    PF_BLUETOOTH(31L),
    PF_ALG(38L),
    PF_VSOCK(40L),
    PF_KCM(41L),
    PF_XDP(44L),
    PF_MAX(45L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 45L;

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
            map.put(PF_LOCAL, "PF_LOCAL");
            map.put(PF_UNIX, "PF_UNIX");
            map.put(PF_INET, "PF_INET");
            map.put(PF_SNA, "PF_SNA");
            map.put(PF_DECnet, "PF_DECnet");
            map.put(PF_APPLETALK, "PF_APPLETALK");
            map.put(PF_ROUTE, "PF_ROUTE");
            map.put(PF_IPX, "PF_IPX");
            map.put(PF_ISDN, "PF_ISDN");
            map.put(PF_KEY, "PF_KEY");
            map.put(PF_INET6, "PF_INET6");
            map.put(PF_NETLINK, "PF_NETLINK");
            map.put(PF_RDS, "PF_RDS");
            map.put(PF_PPPOX, "PF_PPPOX");
            map.put(PF_LLC, "PF_LLC");
            map.put(PF_IB, "PF_IB");
            map.put(PF_MPLS, "PF_MPLS");
            map.put(PF_CAN, "PF_CAN");
            map.put(PF_TIPC, "PF_TIPC");
            map.put(PF_BLUETOOTH, "PF_BLUETOOTH");
            map.put(PF_ALG, "PF_ALG");
            map.put(PF_VSOCK, "PF_VSOCK");
            map.put(PF_KCM, "PF_KCM");
            map.put(PF_XDP, "PF_XDP");
            map.put(PF_MAX, "PF_MAX");
            return map;
        }
    }
}

