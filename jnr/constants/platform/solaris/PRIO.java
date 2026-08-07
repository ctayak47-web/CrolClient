
package jnr.constants.platform.solaris;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum PRIO implements Constant
{
    PRIO_PROCESS(0L),
    PRIO_PGRP(1L),
    PRIO_USER(2L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 2L;

    private PRIO(long value) {
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
        public static final Map<PRIO, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<PRIO, String> generateTable() {
            EnumMap<PRIO, String> map = new EnumMap<PRIO, String>(PRIO.class);
            map.put(PRIO_PROCESS, "PRIO_PROCESS");
            map.put(PRIO_PGRP, "PRIO_PGRP");
            map.put(PRIO_USER, "PRIO_USER");
            return map;
        }
    }
}

