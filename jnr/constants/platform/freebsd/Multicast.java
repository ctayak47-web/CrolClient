
package jnr.constants.platform.freebsd;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Multicast implements Constant
{
    MCAST_JOIN_GROUP(80L),
    MCAST_BLOCK_SOURCE(84L),
    MCAST_UNBLOCK_SOURCE(85L),
    MCAST_LEAVE_GROUP(81L),
    MCAST_JOIN_SOURCE_GROUP(82L),
    MCAST_LEAVE_SOURCE_GROUP(83L),
    MCAST_EXCLUDE(2L),
    MCAST_INCLUDE(1L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 85L;

    private Multicast(long value) {
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
        public static final Map<Multicast, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Multicast, String> generateTable() {
            EnumMap<Multicast, String> map = new EnumMap<Multicast, String>(Multicast.class);
            map.put(MCAST_JOIN_GROUP, "MCAST_JOIN_GROUP");
            map.put(MCAST_BLOCK_SOURCE, "MCAST_BLOCK_SOURCE");
            map.put(MCAST_UNBLOCK_SOURCE, "MCAST_UNBLOCK_SOURCE");
            map.put(MCAST_LEAVE_GROUP, "MCAST_LEAVE_GROUP");
            map.put(MCAST_JOIN_SOURCE_GROUP, "MCAST_JOIN_SOURCE_GROUP");
            map.put(MCAST_LEAVE_SOURCE_GROUP, "MCAST_LEAVE_SOURCE_GROUP");
            map.put(MCAST_EXCLUDE, "MCAST_EXCLUDE");
            map.put(MCAST_INCLUDE, "MCAST_INCLUDE");
            return map;
        }
    }
}

