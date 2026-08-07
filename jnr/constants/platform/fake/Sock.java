
package jnr.constants.platform.fake;

import jnr.constants.Constant;

public enum Sock implements Constant
{
    SOCK_STREAM(1L),
    SOCK_DGRAM(2L),
    SOCK_RAW(3L),
    SOCK_RDM(4L),
    SOCK_SEQPACKET(5L),
    SOCK_NONBLOCK(6L),
    SOCK_CLOEXEC(7L),
    SOCK_MAXADDRLEN(8L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 8L;

    private Sock(long value) {
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

