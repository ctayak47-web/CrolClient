
package jnr.constants.platform.aix;

import jnr.constants.Constant;

public enum TCP implements Constant
{
    TCP_MAX_SACK(4L),
    TCP_MSS(1460L),
    TCP_MAXWIN(65535L),
    TCP_MAXBURST(8L),
    TCP_NODELAY(1L),
    TCP_MAXSEG(2L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 65535L;

    private TCP(long value) {
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

