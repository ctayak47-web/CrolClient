
package jnr.constants.platform.linux.mips64el;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum InterfaceInfo implements Constant
{
    IFF_ALLMULTI(512L),
    IFF_AUTOMEDIA(16384L),
    IFF_BROADCAST(2L),
    IFF_DEBUG(4L),
    IFF_DYNAMIC(32768L),
    IFF_LOOPBACK(8L),
    IFF_MASTER(1024L),
    IFF_MULTICAST(4096L),
    IFF_NOARP(128L),
    IFF_NOTRAILERS(32L),
    IFF_POINTOPOINT(16L),
    IFF_PORTSEL(8192L),
    IFF_PROMISC(256L),
    IFF_RUNNING(64L),
    IFF_SLAVE(2048L),
    IFF_UP(1L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 32768L;

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
            map.put(IFF_ALLMULTI, "IFF_ALLMULTI");
            map.put(IFF_AUTOMEDIA, "IFF_AUTOMEDIA");
            map.put(IFF_BROADCAST, "IFF_BROADCAST");
            map.put(IFF_DEBUG, "IFF_DEBUG");
            map.put(IFF_DYNAMIC, "IFF_DYNAMIC");
            map.put(IFF_LOOPBACK, "IFF_LOOPBACK");
            map.put(IFF_MASTER, "IFF_MASTER");
            map.put(IFF_MULTICAST, "IFF_MULTICAST");
            map.put(IFF_NOARP, "IFF_NOARP");
            map.put(IFF_NOTRAILERS, "IFF_NOTRAILERS");
            map.put(IFF_POINTOPOINT, "IFF_POINTOPOINT");
            map.put(IFF_PORTSEL, "IFF_PORTSEL");
            map.put(IFF_PROMISC, "IFF_PROMISC");
            map.put(IFF_RUNNING, "IFF_RUNNING");
            map.put(IFF_SLAVE, "IFF_SLAVE");
            map.put(IFF_UP, "IFF_UP");
            return map;
        }
    }
}

