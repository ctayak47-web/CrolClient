
package jnr.constants.platform.linux.powerpc64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum SocketControlMessage implements Constant
{
    SCM_RIGHTS(1L),
    SCM_TIMESTAMP(29L),
    SCM_TIMESTAMPNS(35L),
    SCM_TIMESTAMPING(37L),
    SCM_CREDENTIALS(2L),
    SCM_WIFI_STATUS(41L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 41L;

    private SocketControlMessage(long value) {
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
        public static final Map<SocketControlMessage, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<SocketControlMessage, String> generateTable() {
            EnumMap<SocketControlMessage, String> map = new EnumMap<SocketControlMessage, String>(SocketControlMessage.class);
            map.put(SCM_RIGHTS, "SCM_RIGHTS");
            map.put(SCM_TIMESTAMP, "SCM_TIMESTAMP");
            map.put(SCM_TIMESTAMPNS, "SCM_TIMESTAMPNS");
            map.put(SCM_TIMESTAMPING, "SCM_TIMESTAMPING");
            map.put(SCM_CREDENTIALS, "SCM_CREDENTIALS");
            map.put(SCM_WIFI_STATUS, "SCM_WIFI_STATUS");
            return map;
        }
    }
}

