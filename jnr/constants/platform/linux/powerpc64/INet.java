
package jnr.constants.platform.linux.powerpc64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum INet implements Constant
{
    INET_ADDRSTRLEN(16L);

    private final long value;
    public static final long MIN_VALUE = 16L;
    public static final long MAX_VALUE = 16L;

    private INet(long value) {
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
        public static final Map<INet, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<INet, String> generateTable() {
            EnumMap<INet, String> map = new EnumMap<INet, String>(INet.class);
            map.put(INET_ADDRSTRLEN, "INET_ADDRSTRLEN");
            return map;
        }
    }
}

