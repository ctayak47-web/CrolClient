
package jnr.constants.platform.freebsd.aarch64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum PosixFadvise implements Constant
{
    POSIX_FADV_NORMAL(0L),
    POSIX_FADV_SEQUENTIAL(2L),
    POSIX_FADV_RANDOM(1L),
    POSIX_FADV_NOREUSE(5L),
    POSIX_FADV_WILLNEED(3L),
    POSIX_FADV_DONTNEED(4L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 5L;

    private PosixFadvise(long value) {
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
        public static final Map<PosixFadvise, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<PosixFadvise, String> generateTable() {
            EnumMap<PosixFadvise, String> map = new EnumMap<PosixFadvise, String>(PosixFadvise.class);
            map.put(POSIX_FADV_NORMAL, "POSIX_FADV_NORMAL");
            map.put(POSIX_FADV_SEQUENTIAL, "POSIX_FADV_SEQUENTIAL");
            map.put(POSIX_FADV_RANDOM, "POSIX_FADV_RANDOM");
            map.put(POSIX_FADV_NOREUSE, "POSIX_FADV_NOREUSE");
            map.put(POSIX_FADV_WILLNEED, "POSIX_FADV_WILLNEED");
            map.put(POSIX_FADV_DONTNEED, "POSIX_FADV_DONTNEED");
            return map;
        }
    }
}

