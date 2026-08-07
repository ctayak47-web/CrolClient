
package jnr.constants.platform.linux.loongarch64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Confstr implements Constant
{
    _CS_PATH(0L),
    _CS_POSIX_V7_ILP32_OFF32_CFLAGS(1132L),
    _CS_POSIX_V7_ILP32_OFF32_LDFLAGS(1133L),
    _CS_POSIX_V7_ILP32_OFF32_LIBS(1134L),
    _CS_POSIX_V7_ILP32_OFFBIG_CFLAGS(1136L),
    _CS_POSIX_V7_ILP32_OFFBIG_LDFLAGS(1137L),
    _CS_POSIX_V7_ILP32_OFFBIG_LIBS(1138L),
    _CS_POSIX_V7_LP64_OFF64_CFLAGS(1140L),
    _CS_POSIX_V7_LP64_OFF64_LDFLAGS(1141L),
    _CS_POSIX_V7_LP64_OFF64_LIBS(1142L),
    _CS_POSIX_V7_LPBIG_OFFBIG_CFLAGS(1144L),
    _CS_POSIX_V7_LPBIG_OFFBIG_LDFLAGS(1145L),
    _CS_POSIX_V7_LPBIG_OFFBIG_LIBS(1146L),
    _CS_POSIX_V7_WIDTH_RESTRICTED_ENVS(5L),
    _CS_V7_ENV(1149L),
    _CS_POSIX_V6_ILP32_OFF32_CFLAGS(1116L),
    _CS_POSIX_V6_ILP32_OFF32_LDFLAGS(1117L),
    _CS_POSIX_V6_ILP32_OFF32_LIBS(1118L),
    _CS_POSIX_V6_ILP32_OFFBIG_CFLAGS(1120L),
    _CS_POSIX_V6_ILP32_OFFBIG_LDFLAGS(1121L),
    _CS_POSIX_V6_ILP32_OFFBIG_LIBS(1122L),
    _CS_POSIX_V6_LP64_OFF64_CFLAGS(1124L),
    _CS_POSIX_V6_LP64_OFF64_LDFLAGS(1125L),
    _CS_POSIX_V6_LP64_OFF64_LIBS(1126L),
    _CS_POSIX_V6_LPBIG_OFFBIG_CFLAGS(1128L),
    _CS_POSIX_V6_LPBIG_OFFBIG_LDFLAGS(1129L),
    _CS_POSIX_V6_LPBIG_OFFBIG_LIBS(1130L),
    _CS_POSIX_V6_WIDTH_RESTRICTED_ENVS(1L),
    _CS_V6_ENV(1148L),
    _CS_GNU_LIBC_VERSION(2L),
    _CS_GNU_LIBPTHREAD_VERSION(3L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 1149L;

    private Confstr(long value) {
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
        public static final Map<Confstr, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Confstr, String> generateTable() {
            EnumMap<Confstr, String> map = new EnumMap<Confstr, String>(Confstr.class);
            map.put(_CS_PATH, "_CS_PATH");
            map.put(_CS_POSIX_V7_ILP32_OFF32_CFLAGS, "_CS_POSIX_V7_ILP32_OFF32_CFLAGS");
            map.put(_CS_POSIX_V7_ILP32_OFF32_LDFLAGS, "_CS_POSIX_V7_ILP32_OFF32_LDFLAGS");
            map.put(_CS_POSIX_V7_ILP32_OFF32_LIBS, "_CS_POSIX_V7_ILP32_OFF32_LIBS");
            map.put(_CS_POSIX_V7_ILP32_OFFBIG_CFLAGS, "_CS_POSIX_V7_ILP32_OFFBIG_CFLAGS");
            map.put(_CS_POSIX_V7_ILP32_OFFBIG_LDFLAGS, "_CS_POSIX_V7_ILP32_OFFBIG_LDFLAGS");
            map.put(_CS_POSIX_V7_ILP32_OFFBIG_LIBS, "_CS_POSIX_V7_ILP32_OFFBIG_LIBS");
            map.put(_CS_POSIX_V7_LP64_OFF64_CFLAGS, "_CS_POSIX_V7_LP64_OFF64_CFLAGS");
            map.put(_CS_POSIX_V7_LP64_OFF64_LDFLAGS, "_CS_POSIX_V7_LP64_OFF64_LDFLAGS");
            map.put(_CS_POSIX_V7_LP64_OFF64_LIBS, "_CS_POSIX_V7_LP64_OFF64_LIBS");
            map.put(_CS_POSIX_V7_LPBIG_OFFBIG_CFLAGS, "_CS_POSIX_V7_LPBIG_OFFBIG_CFLAGS");
            map.put(_CS_POSIX_V7_LPBIG_OFFBIG_LDFLAGS, "_CS_POSIX_V7_LPBIG_OFFBIG_LDFLAGS");
            map.put(_CS_POSIX_V7_LPBIG_OFFBIG_LIBS, "_CS_POSIX_V7_LPBIG_OFFBIG_LIBS");
            map.put(_CS_POSIX_V7_WIDTH_RESTRICTED_ENVS, "_CS_POSIX_V7_WIDTH_RESTRICTED_ENVS");
            map.put(_CS_V7_ENV, "_CS_V7_ENV");
            map.put(_CS_POSIX_V6_ILP32_OFF32_CFLAGS, "_CS_POSIX_V6_ILP32_OFF32_CFLAGS");
            map.put(_CS_POSIX_V6_ILP32_OFF32_LDFLAGS, "_CS_POSIX_V6_ILP32_OFF32_LDFLAGS");
            map.put(_CS_POSIX_V6_ILP32_OFF32_LIBS, "_CS_POSIX_V6_ILP32_OFF32_LIBS");
            map.put(_CS_POSIX_V6_ILP32_OFFBIG_CFLAGS, "_CS_POSIX_V6_ILP32_OFFBIG_CFLAGS");
            map.put(_CS_POSIX_V6_ILP32_OFFBIG_LDFLAGS, "_CS_POSIX_V6_ILP32_OFFBIG_LDFLAGS");
            map.put(_CS_POSIX_V6_ILP32_OFFBIG_LIBS, "_CS_POSIX_V6_ILP32_OFFBIG_LIBS");
            map.put(_CS_POSIX_V6_LP64_OFF64_CFLAGS, "_CS_POSIX_V6_LP64_OFF64_CFLAGS");
            map.put(_CS_POSIX_V6_LP64_OFF64_LDFLAGS, "_CS_POSIX_V6_LP64_OFF64_LDFLAGS");
            map.put(_CS_POSIX_V6_LP64_OFF64_LIBS, "_CS_POSIX_V6_LP64_OFF64_LIBS");
            map.put(_CS_POSIX_V6_LPBIG_OFFBIG_CFLAGS, "_CS_POSIX_V6_LPBIG_OFFBIG_CFLAGS");
            map.put(_CS_POSIX_V6_LPBIG_OFFBIG_LDFLAGS, "_CS_POSIX_V6_LPBIG_OFFBIG_LDFLAGS");
            map.put(_CS_POSIX_V6_LPBIG_OFFBIG_LIBS, "_CS_POSIX_V6_LPBIG_OFFBIG_LIBS");
            map.put(_CS_POSIX_V6_WIDTH_RESTRICTED_ENVS, "_CS_POSIX_V6_WIDTH_RESTRICTED_ENVS");
            map.put(_CS_V6_ENV, "_CS_V6_ENV");
            map.put(_CS_GNU_LIBC_VERSION, "_CS_GNU_LIBC_VERSION");
            map.put(_CS_GNU_LIBPTHREAD_VERSION, "_CS_GNU_LIBPTHREAD_VERSION");
            return map;
        }
    }
}

