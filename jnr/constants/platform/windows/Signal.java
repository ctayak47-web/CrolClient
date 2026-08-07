
package jnr.constants.platform.windows;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Signal implements Constant
{
    SIGINT(2L),
    SIGILL(4L),
    SIGABRT(22L),
    SIGFPE(8L),
    SIGSEGV(11L),
    SIGTERM(15L),
    NSIG(23L);

    private final long value;
    public static final long MIN_VALUE = 2L;
    public static final long MAX_VALUE = 23L;

    private Signal(long value) {
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
        public static final Map<Signal, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Signal, String> generateTable() {
            EnumMap<Signal, String> map = new EnumMap<Signal, String>(Signal.class);
            map.put(SIGINT, "SIGINT");
            map.put(SIGILL, "SIGILL");
            map.put(SIGABRT, "SIGABRT");
            map.put(SIGFPE, "SIGFPE");
            map.put(SIGSEGV, "SIGSEGV");
            map.put(SIGTERM, "SIGTERM");
            map.put(NSIG, "NSIG");
            return map;
        }
    }
}

