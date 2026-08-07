
package jnr.constants.platform.linux.loongarch64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum SocketMessage implements Constant
{
    MSG_DONTWAIT(64L),
    MSG_OOB(1L),
    MSG_PEEK(2L),
    MSG_DONTROUTE(4L),
    MSG_EOR(128L),
    MSG_TRUNC(32L),
    MSG_CTRUNC(8L),
    MSG_WAITALL(256L),
    MSG_PROXY(16L),
    MSG_FIN(512L),
    MSG_SYN(1024L),
    MSG_CONFIRM(2048L),
    MSG_RST(4096L),
    MSG_ERRQUEUE(8192L),
    MSG_NOSIGNAL(16384L),
    MSG_MORE(32768L),
    MSG_FASTOPEN(0x20000000L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 0x20000000L;

    private SocketMessage(long value) {
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
        public static final Map<SocketMessage, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<SocketMessage, String> generateTable() {
            EnumMap<SocketMessage, String> map = new EnumMap<SocketMessage, String>(SocketMessage.class);
            map.put(MSG_DONTWAIT, "MSG_DONTWAIT");
            map.put(MSG_OOB, "MSG_OOB");
            map.put(MSG_PEEK, "MSG_PEEK");
            map.put(MSG_DONTROUTE, "MSG_DONTROUTE");
            map.put(MSG_EOR, "MSG_EOR");
            map.put(MSG_TRUNC, "MSG_TRUNC");
            map.put(MSG_CTRUNC, "MSG_CTRUNC");
            map.put(MSG_WAITALL, "MSG_WAITALL");
            map.put(MSG_PROXY, "MSG_PROXY");
            map.put(MSG_FIN, "MSG_FIN");
            map.put(MSG_SYN, "MSG_SYN");
            map.put(MSG_CONFIRM, "MSG_CONFIRM");
            map.put(MSG_RST, "MSG_RST");
            map.put(MSG_ERRQUEUE, "MSG_ERRQUEUE");
            map.put(MSG_NOSIGNAL, "MSG_NOSIGNAL");
            map.put(MSG_MORE, "MSG_MORE");
            map.put(MSG_FASTOPEN, "MSG_FASTOPEN");
            return map;
        }
    }
}

