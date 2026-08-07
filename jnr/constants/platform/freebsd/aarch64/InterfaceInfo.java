
package jnr.constants.platform.freebsd.aarch64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum InterfaceInfo implements Constant
{
    IFF_ALLMULTI(512L),
    IFF_ALTPHYS(16384L),
    IFF_BROADCAST(2L),
    IFF_CANTCONFIG(65536L),
    IFF_DEBUG(4L),
    IFF_DRV_OACTIVE(1024L),
    IFF_DRV_RUNNING(64L),
    IFF_DYING(0x200000L),
    IFF_LINK0(4096L),
    IFF_LINK1(8192L),
    IFF_LINK2(16384L),
    IFF_LOOPBACK(8L),
    IFF_MONITOR(262144L),
    IFF_MULTICAST(32768L),
    IFF_NOARP(128L),
    IFF_OACTIVE(1024L),
    IFF_POINTOPOINT(16L),
    IFF_PPROMISC(131072L),
    IFF_PROMISC(256L),
    IFF_RENAMING(0x400000L),
    IFF_RUNNING(64L),
    IFF_SIMPLEX(2048L),
    IFF_STATICARP(524288L),
    IFF_UP(1L),
    IFF_CANTCHANGE(2199410L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 0x400000L;

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
            map.put(IFF_ALTPHYS, "IFF_ALTPHYS");
            map.put(IFF_BROADCAST, "IFF_BROADCAST");
            map.put(IFF_CANTCONFIG, "IFF_CANTCONFIG");
            map.put(IFF_DEBUG, "IFF_DEBUG");
            map.put(IFF_DRV_OACTIVE, "IFF_DRV_OACTIVE");
            map.put(IFF_DRV_RUNNING, "IFF_DRV_RUNNING");
            map.put(IFF_DYING, "IFF_DYING");
            map.put(IFF_LINK0, "IFF_LINK0");
            map.put(IFF_LINK1, "IFF_LINK1");
            map.put(IFF_LINK2, "IFF_LINK2");
            map.put(IFF_LOOPBACK, "IFF_LOOPBACK");
            map.put(IFF_MONITOR, "IFF_MONITOR");
            map.put(IFF_MULTICAST, "IFF_MULTICAST");
            map.put(IFF_NOARP, "IFF_NOARP");
            map.put(IFF_OACTIVE, "IFF_OACTIVE");
            map.put(IFF_POINTOPOINT, "IFF_POINTOPOINT");
            map.put(IFF_PPROMISC, "IFF_PPROMISC");
            map.put(IFF_PROMISC, "IFF_PROMISC");
            map.put(IFF_RENAMING, "IFF_RENAMING");
            map.put(IFF_RUNNING, "IFF_RUNNING");
            map.put(IFF_SIMPLEX, "IFF_SIMPLEX");
            map.put(IFF_STATICARP, "IFF_STATICARP");
            map.put(IFF_UP, "IFF_UP");
            map.put(IFF_CANTCHANGE, "IFF_CANTCHANGE");
            return map;
        }
    }
}

