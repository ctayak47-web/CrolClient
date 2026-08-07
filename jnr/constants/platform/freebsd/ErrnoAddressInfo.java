
package jnr.constants.platform.freebsd;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum ErrnoAddressInfo implements Constant
{
    EAI_AGAIN(2L),
    EAI_BADFLAGS(3L),
    EAI_FAIL(4L),
    EAI_FAMILY(5L),
    EAI_MEMORY(6L),
    EAI_NONAME(8L),
    EAI_OVERFLOW(14L),
    EAI_SERVICE(9L),
    EAI_SOCKTYPE(10L),
    EAI_SYSTEM(11L),
    EAI_BADHINTS(12L),
    EAI_PROTOCOL(13L),
    EAI_MAX(15L);

    private final long value;
    public static final long MIN_VALUE = 2L;
    public static final long MAX_VALUE = 15L;

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
            map.put(EAI_AGAIN, "EAI_AGAIN");
            map.put(EAI_BADFLAGS, "EAI_BADFLAGS");
            map.put(EAI_FAIL, "EAI_FAIL");
            map.put(EAI_FAMILY, "EAI_FAMILY");
            map.put(EAI_MEMORY, "EAI_MEMORY");
            map.put(EAI_NONAME, "EAI_NONAME");
            map.put(EAI_OVERFLOW, "EAI_OVERFLOW");
            map.put(EAI_SERVICE, "EAI_SERVICE");
            map.put(EAI_SOCKTYPE, "EAI_SOCKTYPE");
            map.put(EAI_SYSTEM, "EAI_SYSTEM");
            map.put(EAI_BADHINTS, "EAI_BADHINTS");
            map.put(EAI_PROTOCOL, "EAI_PROTOCOL");
            map.put(EAI_MAX, "EAI_MAX");
            return map;
        }
    }
}

