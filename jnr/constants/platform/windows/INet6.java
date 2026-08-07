
package jnr.constants.platform.windows;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum INet6 implements Constant
{
    INET6_ADDRSTRLEN(65L);

    private final long value;
    public static final long MIN_VALUE = 65L;
    public static final long MAX_VALUE = 65L;

    private INet6(long value) {
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
        public static final Map<INet6, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<INet6, String> generateTable() {
            EnumMap<INet6, String> map = new EnumMap<INet6, String>(INet6.class);
            map.put(INET6_ADDRSTRLEN, "INET6_ADDRSTRLEN");
            return map;
        }
    }
}

