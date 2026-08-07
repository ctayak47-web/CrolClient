
package jnr.constants.platform.openbsd;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Locale implements Constant
{
    LC_CTYPE(2L),
    LC_NUMERIC(4L),
    LC_TIME(5L),
    LC_COLLATE(1L),
    LC_MONETARY(3L),
    LC_MESSAGES(6L),
    LC_ALL(0L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 6L;

    private Locale(long value) {
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
        public static final Map<Locale, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Locale, String> generateTable() {
            EnumMap<Locale, String> map = new EnumMap<Locale, String>(Locale.class);
            map.put(LC_CTYPE, "LC_CTYPE");
            map.put(LC_NUMERIC, "LC_NUMERIC");
            map.put(LC_TIME, "LC_TIME");
            map.put(LC_COLLATE, "LC_COLLATE");
            map.put(LC_MONETARY, "LC_MONETARY");
            map.put(LC_MESSAGES, "LC_MESSAGES");
            map.put(LC_ALL, "LC_ALL");
            return map;
        }
    }
}

