
package jnr.constants.platform.aix;

import jnr.constants.Constant;

public enum RLIM implements Constant
{
    RLIM_NLIMITS(10L),
    RLIM_INFINITY(Long.MAX_VALUE),
    RLIM_SAVED_MAX(0x7FFFFFFFFFFFFFFEL),
    RLIM_SAVED_CUR(0x7FFFFFFFFFFFFFFDL);

    private final long value;
    public static final long MIN_VALUE = 10L;
    public static final long MAX_VALUE = Long.MAX_VALUE;

    private RLIM(long value) {
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

