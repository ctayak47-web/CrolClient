
package jnr.constants.platform.fake;

import jnr.constants.Constant;

public enum INet6 implements Constant
{
    INET6_ADDRSTRLEN(1L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 1L;

    private INet6(long value) {
        this.value = value;
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
}

