
package jnr.constants.platform.darwin;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum IPv6 implements Constant
{
    IPV6_JOIN_GROUP(12L),
    IPV6_LEAVE_GROUP(13L),
    IPV6_MULTICAST_HOPS(10L),
    IPV6_MULTICAST_IF(9L),
    IPV6_MULTICAST_LOOP(11L),
    IPV6_UNICAST_HOPS(4L),
    IPV6_V6ONLY(27L),
    IPV6_CHECKSUM(26L),
    IPV6_RECVTCLASS(35L),
    IPV6_RTHDR_TYPE_0(0L),
    IPV6_TCLASS(36L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 36L;

    private IPv6(long value) {
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
        public static final Map<IPv6, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<IPv6, String> generateTable() {
            EnumMap<IPv6, String> map = new EnumMap<IPv6, String>(IPv6.class);
            map.put(IPV6_JOIN_GROUP, "IPV6_JOIN_GROUP");
            map.put(IPV6_LEAVE_GROUP, "IPV6_LEAVE_GROUP");
            map.put(IPV6_MULTICAST_HOPS, "IPV6_MULTICAST_HOPS");
            map.put(IPV6_MULTICAST_IF, "IPV6_MULTICAST_IF");
            map.put(IPV6_MULTICAST_LOOP, "IPV6_MULTICAST_LOOP");
            map.put(IPV6_UNICAST_HOPS, "IPV6_UNICAST_HOPS");
            map.put(IPV6_V6ONLY, "IPV6_V6ONLY");
            map.put(IPV6_CHECKSUM, "IPV6_CHECKSUM");
            map.put(IPV6_RECVTCLASS, "IPV6_RECVTCLASS");
            map.put(IPV6_RTHDR_TYPE_0, "IPV6_RTHDR_TYPE_0");
            map.put(IPV6_TCLASS, "IPV6_TCLASS");
            return map;
        }
    }
}

