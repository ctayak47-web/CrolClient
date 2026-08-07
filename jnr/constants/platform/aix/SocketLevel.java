
package jnr.constants.platform.aix;

import jnr.constants.Constant;

public enum SocketLevel implements Constant
{
    SOL_SOCKET(65535L);

    private final long value;
    public static final long MIN_VALUE = 65535L;
    public static final long MAX_VALUE = 65535L;

    private SocketLevel(long value) {
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

