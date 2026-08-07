
package jnr.constants.platform.linux.powerpc64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum OpenFlags implements Constant
{
    O_RDONLY(0L),
    O_WRONLY(1L),
    O_RDWR(2L),
    O_ACCMODE(3L),
    O_NONBLOCK(2048L),
    O_APPEND(1024L),
    O_SYNC(0x101000L),
    O_ASYNC(8192L),
    O_FSYNC(0x101000L),
    O_NOFOLLOW(32768L),
    O_CREAT(64L),
    O_TRUNC(512L),
    O_EXCL(128L),
    O_DIRECTORY(16384L),
    O_NOCTTY(256L),
    O_TMPFILE(0x404000L),
    O_CLOEXEC(524288L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 0x404000L;

    private OpenFlags(long value) {
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
        public static final Map<OpenFlags, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<OpenFlags, String> generateTable() {
            EnumMap<OpenFlags, String> map = new EnumMap<OpenFlags, String>(OpenFlags.class);
            map.put(O_RDONLY, "O_RDONLY");
            map.put(O_WRONLY, "O_WRONLY");
            map.put(O_RDWR, "O_RDWR");
            map.put(O_ACCMODE, "O_ACCMODE");
            map.put(O_NONBLOCK, "O_NONBLOCK");
            map.put(O_APPEND, "O_APPEND");
            map.put(O_SYNC, "O_SYNC");
            map.put(O_ASYNC, "O_ASYNC");
            map.put(O_FSYNC, "O_FSYNC");
            map.put(O_NOFOLLOW, "O_NOFOLLOW");
            map.put(O_CREAT, "O_CREAT");
            map.put(O_TRUNC, "O_TRUNC");
            map.put(O_EXCL, "O_EXCL");
            map.put(O_DIRECTORY, "O_DIRECTORY");
            map.put(O_NOCTTY, "O_NOCTTY");
            map.put(O_TMPFILE, "O_TMPFILE");
            map.put(O_CLOEXEC, "O_CLOEXEC");
            return map;
        }
    }
}

