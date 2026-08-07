
package jnr.constants.platform.dragonflybsd;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum INAddr implements Constant
{
    INADDR_ANY(0L),
    INADDR_BROADCAST(0xFFFFFFFFL),
    INADDR_NONE(0xFFFFFFFFL),
    INADDR_LOOPBACK(2130706433L),
    INADDR_UNSPEC_GROUP(0xE0000000L),
    INADDR_ALLHOSTS_GROUP(0xE0000001L),
    INADDR_ALLRTRS_GROUP(0xE0000002L),
    INADDR_MAX_LOCAL_GROUP(0xE00000FFL);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 0xFFFFFFFFL;

    private INAddr(long value) {
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
        public static final Map<INAddr, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<INAddr, String> generateTable() {
            EnumMap<INAddr, String> map = new EnumMap<INAddr, String>(INAddr.class);
            map.put(INADDR_ANY, "INADDR_ANY");
            map.put(INADDR_BROADCAST, "INADDR_BROADCAST");
            map.put(INADDR_NONE, "INADDR_NONE");
            map.put(INADDR_LOOPBACK, "INADDR_LOOPBACK");
            map.put(INADDR_UNSPEC_GROUP, "INADDR_UNSPEC_GROUP");
            map.put(INADDR_ALLHOSTS_GROUP, "INADDR_ALLHOSTS_GROUP");
            map.put(INADDR_ALLRTRS_GROUP, "INADDR_ALLRTRS_GROUP");
            map.put(INADDR_MAX_LOCAL_GROUP, "INADDR_MAX_LOCAL_GROUP");
            return map;
        }
    }
}

