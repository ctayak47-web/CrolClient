
package jnr.constants.platform.aix;

import jnr.constants.Constant;

public enum WaitFlags implements Constant
{
    WNOHANG(1L),
    WUNTRACED(2L),
    WSTOPPED(64L),
    WEXITED(4L),
    WCONTINUED(0x1000000L),
    WNOWAIT(16L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 0x1000000L;

    private WaitFlags(long value) {
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

