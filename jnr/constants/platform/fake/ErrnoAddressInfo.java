
package jnr.constants.platform.fake;

import jnr.constants.Constant;

public enum ErrnoAddressInfo implements Constant
{
    EAI_ADDRFAMILY(1L),
    EAI_AGAIN(2L),
    EAI_BADFLAGS(3L),
    EAI_FAIL(4L),
    EAI_FAMILY(5L),
    EAI_MEMORY(6L),
    EAI_NODATA(7L),
    EAI_NONAME(8L),
    EAI_OVERFLOW(9L),
    EAI_SERVICE(10L),
    EAI_SOCKTYPE(11L),
    EAI_SYSTEM(12L),
    EAI_BADHINTS(13L),
    EAI_PROTOCOL(14L),
    EAI_MAX(15L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 15L;

    private ErrnoAddressInfo(long value) {
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

