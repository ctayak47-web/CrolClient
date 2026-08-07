
package jnr.constants.platform.freebsd.aarch64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Local implements Constant
{
    LOCAL_PEERCRED(1L),
    LOCAL_CREDS(2L),
    LOCAL_CONNWAIT(4L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 4L;

    private Local(long value) {
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
        public static final Map<Local, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Local, String> generateTable() {
            EnumMap<Local, String> map = new EnumMap<Local, String>(Local.class);
            map.put(LOCAL_PEERCRED, "LOCAL_PEERCRED");
            map.put(LOCAL_CREDS, "LOCAL_CREDS");
            map.put(LOCAL_CONNWAIT, "LOCAL_CONNWAIT");
            return map;
        }
    }
}

