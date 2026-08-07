
package jnr.constants.platform.linux;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum ErrnoAddressInfo implements Constant
{
    EAI_ADDRFAMILY(-9L),
    EAI_AGAIN(-3L),
    EAI_BADFLAGS(-1L),
    EAI_FAIL(-4L),
    EAI_FAMILY(-6L),
    EAI_MEMORY(-10L),
    EAI_NODATA(-5L),
    EAI_NONAME(-2L),
    EAI_OVERFLOW(-12L),
    EAI_SERVICE(-8L),
    EAI_SOCKTYPE(-7L),
    EAI_SYSTEM(-11L);

    private final long value;
    public static final long MIN_VALUE = -12L;
    public static final long MAX_VALUE = -1L;

    private ErrnoAddressInfo(long value) {
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
        public static final Map<ErrnoAddressInfo, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<ErrnoAddressInfo, String> generateTable() {
            EnumMap<ErrnoAddressInfo, String> map = new EnumMap<ErrnoAddressInfo, String>(ErrnoAddressInfo.class);
            map.put(EAI_ADDRFAMILY, "EAI_ADDRFAMILY");
            map.put(EAI_AGAIN, "EAI_AGAIN");
            map.put(EAI_BADFLAGS, "EAI_BADFLAGS");
            map.put(EAI_FAIL, "EAI_FAIL");
            map.put(EAI_FAMILY, "EAI_FAMILY");
            map.put(EAI_MEMORY, "EAI_MEMORY");
            map.put(EAI_NODATA, "EAI_NODATA");
            map.put(EAI_NONAME, "EAI_NONAME");
            map.put(EAI_OVERFLOW, "EAI_OVERFLOW");
            map.put(EAI_SERVICE, "EAI_SERVICE");
            map.put(EAI_SOCKTYPE, "EAI_SOCKTYPE");
            map.put(EAI_SYSTEM, "EAI_SYSTEM");
            return map;
        }
    }
}

