
package jnr.constants.platform.linux.s390x;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Fcntl implements Constant
{
    FAPPEND(1024L),
    FASYNC(8192L),
    FFSYNC(0x101000L),
    FNONBLOCK(2048L),
    FNDELAY(2048L),
    F_DUPFD(0L),
    F_GETFD(1L),
    F_SETFD(2L),
    F_GETFL(3L),
    F_SETFL(4L),
    F_GETOWN(9L),
    F_SETOWN(8L),
    F_GETLK(5L),
    F_SETLK(6L),
    F_SETLKW(7L),
    F_RDLCK(0L),
    F_UNLCK(2L),
    F_WRLCK(1L),
    F_GETPIPE_SZ(1032L),
    F_SETPIPE_SZ(1031L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 0x101000L;

    private Fcntl(long value) {
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
        public static final Map<Fcntl, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Fcntl, String> generateTable() {
            EnumMap<Fcntl, String> map = new EnumMap<Fcntl, String>(Fcntl.class);
            map.put(FAPPEND, "FAPPEND");
            map.put(FASYNC, "FASYNC");
            map.put(FFSYNC, "FFSYNC");
            map.put(FNONBLOCK, "FNONBLOCK");
            map.put(FNDELAY, "FNDELAY");
            map.put(F_DUPFD, "F_DUPFD");
            map.put(F_GETFD, "F_GETFD");
            map.put(F_SETFD, "F_SETFD");
            map.put(F_GETFL, "F_GETFL");
            map.put(F_SETFL, "F_SETFL");
            map.put(F_GETOWN, "F_GETOWN");
            map.put(F_SETOWN, "F_SETOWN");
            map.put(F_GETLK, "F_GETLK");
            map.put(F_SETLK, "F_SETLK");
            map.put(F_SETLKW, "F_SETLKW");
            map.put(F_RDLCK, "F_RDLCK");
            map.put(F_UNLCK, "F_UNLCK");
            map.put(F_WRLCK, "F_WRLCK");
            map.put(F_GETPIPE_SZ, "F_GETPIPE_SZ");
            map.put(F_SETPIPE_SZ, "F_SETPIPE_SZ");
            return map;
        }
    }
}

