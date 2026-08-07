
package jnr.constants.platform.windows;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum InterfaceInfo implements Constant
{
    IFF_BROADCAST(2L),
    IFF_LOOPBACK(4L),
    IFF_MULTICAST(16L),
    IFF_UP(1L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 16L;

    private InterfaceInfo(long value) {
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
        public static final Map<InterfaceInfo, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<InterfaceInfo, String> generateTable() {
            EnumMap<InterfaceInfo, String> map = new EnumMap<InterfaceInfo, String>(InterfaceInfo.class);
            map.put(IFF_BROADCAST, "IFF_BROADCAST");
            map.put(IFF_LOOPBACK, "IFF_LOOPBACK");
            map.put(IFF_MULTICAST, "IFF_MULTICAST");
            map.put(IFF_UP, "IFF_UP");
            return map;
        }
    }
}

