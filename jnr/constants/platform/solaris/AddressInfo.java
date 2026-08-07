
package jnr.constants.platform.solaris;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum AddressInfo implements Constant
{
    AI_PASSIVE(8L),
    AI_CANONNAME(16L),
    AI_NUMERICHOST(32L),
    AI_NUMERICSERV(64L),
    AI_ALL(2L),
    AI_ADDRCONFIG(4L),
    AI_V4MAPPED(1L),
    AI_DEFAULT(5L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 64L;

    private AddressInfo(long value) {
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
        public static final Map<AddressInfo, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<AddressInfo, String> generateTable() {
            EnumMap<AddressInfo, String> map = new EnumMap<AddressInfo, String>(AddressInfo.class);
            map.put(AI_PASSIVE, "AI_PASSIVE");
            map.put(AI_CANONNAME, "AI_CANONNAME");
            map.put(AI_NUMERICHOST, "AI_NUMERICHOST");
            map.put(AI_NUMERICSERV, "AI_NUMERICSERV");
            map.put(AI_ALL, "AI_ALL");
            map.put(AI_ADDRCONFIG, "AI_ADDRCONFIG");
            map.put(AI_V4MAPPED, "AI_V4MAPPED");
            map.put(AI_DEFAULT, "AI_DEFAULT");
            return map;
        }
    }
}

