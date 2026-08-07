
package jnr.constants.platform.solaris;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum RLIMIT implements Constant
{
    RLIMIT_AS(6L),
    RLIMIT_CORE(4L),
    RLIMIT_CPU(0L),
    RLIMIT_DATA(2L),
    RLIMIT_FSIZE(1L),
    RLIMIT_NOFILE(5L),
    RLIMIT_STACK(3L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 6L;

    private RLIMIT(long value) {
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
        public static final Map<RLIMIT, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<RLIMIT, String> generateTable() {
            EnumMap<RLIMIT, String> map = new EnumMap<RLIMIT, String>(RLIMIT.class);
            map.put(RLIMIT_AS, "RLIMIT_AS");
            map.put(RLIMIT_CORE, "RLIMIT_CORE");
            map.put(RLIMIT_CPU, "RLIMIT_CPU");
            map.put(RLIMIT_DATA, "RLIMIT_DATA");
            map.put(RLIMIT_FSIZE, "RLIMIT_FSIZE");
            map.put(RLIMIT_NOFILE, "RLIMIT_NOFILE");
            map.put(RLIMIT_STACK, "RLIMIT_STACK");
            return map;
        }
    }
}

