
package jnr.constants.platform.linux;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Access implements Constant
{
    F_OK(0L),
    X_OK(1L),
    W_OK(2L),
    R_OK(4L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 4L;

    private Access(long value) {
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
        public static final Map<Access, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Access, String> generateTable() {
            EnumMap<Access, String> map = new EnumMap<Access, String>(Access.class);
            map.put(F_OK, "F_OK");
            map.put(X_OK, "X_OK");
            map.put(W_OK, "W_OK");
            map.put(R_OK, "R_OK");
            return map;
        }
    }
}

