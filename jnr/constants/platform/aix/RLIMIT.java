
package jnr.constants.platform.aix;

import jnr.constants.Constant;

public enum RLIMIT implements Constant
{
    RLIMIT_AS(6L),
    RLIMIT_CORE(4L),
    RLIMIT_CPU(0L),
    RLIMIT_DATA(2L),
    RLIMIT_FSIZE(1L),
    RLIMIT_NOFILE(7L),
    RLIMIT_NPROC(9L),
    RLIMIT_RSS(5L),
    RLIMIT_STACK(3L);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 9L;

    private RLIMIT(long value) {
        this.value = value;
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
}

