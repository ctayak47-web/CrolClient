
package jnr.constants.platform.solaris;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum IP implements Constant
{
    IP_OPTIONS(1L),
    IP_HDRINCL(2L),
    IP_TOS(3L),
    IP_TTL(4L),
    IP_RECVOPTS(5L),
    IP_RECVRETOPTS(6L),
    IP_RECVDSTADDR(7L),
    IP_RETOPTS(8L),
    IP_DONTFRAG(27L),
    IP_RECVTTL(11L),
    IP_RECVIF(9L),
    IP_RECVSLLA(10L),
    IP_MULTICAST_IF(16L),
    IP_MULTICAST_TTL(17L),
    IP_MULTICAST_LOOP(18L),
    IP_ADD_MEMBERSHIP(19L),
    IP_DROP_MEMBERSHIP(20L),
    IP_DEFAULT_MULTICAST_TTL(1L),
    IP_DEFAULT_MULTICAST_LOOP(1L),
    IP_PKTINFO(26L),
    IP_UNBLOCK_SOURCE(22L),
    IP_BLOCK_SOURCE(21L),
    IP_ADD_SOURCE_MEMBERSHIP(23L),
    IP_DROP_SOURCE_MEMBERSHIP(24L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 27L;

    private IP(long value) {
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
        public static final Map<IP, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<IP, String> generateTable() {
            EnumMap<IP, String> map = new EnumMap<IP, String>(IP.class);
            map.put(IP_OPTIONS, "IP_OPTIONS");
            map.put(IP_HDRINCL, "IP_HDRINCL");
            map.put(IP_TOS, "IP_TOS");
            map.put(IP_TTL, "IP_TTL");
            map.put(IP_RECVOPTS, "IP_RECVOPTS");
            map.put(IP_RECVRETOPTS, "IP_RECVRETOPTS");
            map.put(IP_RECVDSTADDR, "IP_RECVDSTADDR");
            map.put(IP_RETOPTS, "IP_RETOPTS");
            map.put(IP_DONTFRAG, "IP_DONTFRAG");
            map.put(IP_RECVTTL, "IP_RECVTTL");
            map.put(IP_RECVIF, "IP_RECVIF");
            map.put(IP_RECVSLLA, "IP_RECVSLLA");
            map.put(IP_MULTICAST_IF, "IP_MULTICAST_IF");
            map.put(IP_MULTICAST_TTL, "IP_MULTICAST_TTL");
            map.put(IP_MULTICAST_LOOP, "IP_MULTICAST_LOOP");
            map.put(IP_ADD_MEMBERSHIP, "IP_ADD_MEMBERSHIP");
            map.put(IP_DROP_MEMBERSHIP, "IP_DROP_MEMBERSHIP");
            map.put(IP_DEFAULT_MULTICAST_TTL, "IP_DEFAULT_MULTICAST_TTL");
            map.put(IP_DEFAULT_MULTICAST_LOOP, "IP_DEFAULT_MULTICAST_LOOP");
            map.put(IP_PKTINFO, "IP_PKTINFO");
            map.put(IP_UNBLOCK_SOURCE, "IP_UNBLOCK_SOURCE");
            map.put(IP_BLOCK_SOURCE, "IP_BLOCK_SOURCE");
            map.put(IP_ADD_SOURCE_MEMBERSHIP, "IP_ADD_SOURCE_MEMBERSHIP");
            map.put(IP_DROP_SOURCE_MEMBERSHIP, "IP_DROP_SOURCE_MEMBERSHIP");
            return map;
        }
    }
}

