
package jnr.constants.platform.aix;

import jnr.constants.Constant;

public enum INAddr implements Constant
{
    INADDR_ANY(0L),
    INADDR_BROADCAST(0xFFFFFFFFL),
    INADDR_NONE(0xFFFFFFFFL),
    INADDR_LOOPBACK(2130706433L),
    INADDR_UNSPEC_GROUP(0xE0000000L),
    INADDR_ALLHOSTS_GROUP(0xE0000001L),
    INADDR_ALLRTRS_GROUP(0xE0000002L),
    INADDR_MAX_LOCAL_GROUP(0xE00000FFL);

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 0xFFFFFFFFL;

    private INAddr(long value) {
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

