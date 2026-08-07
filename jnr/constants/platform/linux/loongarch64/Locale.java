
package jnr.constants.platform.linux.loongarch64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Locale implements Constant
{
    LC_CTYPE(0L),
    LC_NUMERIC(1L),
    LC_TIME(2L),
    LC_COLLATE(3L),
    LC_MONETARY(4L),
    LC_MESSAGES(5L),
    LC_ALL(6L),
    LC_PAPER(7L),
    LC_NAME(8L),
    LC_ADDRESS(9L),
    LC_TELEPHONE(10L),
    LC_MEASUREMENT(11L),
    LC_IDENTIFICATION(12L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 12L;

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
            map.put(LC_PAPER, "LC_PAPER");
            map.put(LC_NAME, "LC_NAME");
            map.put(LC_ADDRESS, "LC_ADDRESS");
            map.put(LC_TELEPHONE, "LC_TELEPHONE");
            map.put(LC_MEASUREMENT, "LC_MEASUREMENT");
            map.put(LC_IDENTIFICATION, "LC_IDENTIFICATION");
            return map;
        }
    }
}

