
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum ErrnoAddressInfo implements Constant
{
    EAI_ADDRFAMILY,
    EAI_AGAIN,
    EAI_BADFLAGS,
    EAI_FAIL,
    EAI_FAMILY,
    EAI_MEMORY,
    EAI_NODATA,
    EAI_NONAME,
    EAI_OVERFLOW,
    EAI_SERVICE,
    EAI_SOCKTYPE,
    EAI_SYSTEM,
    EAI_BADHINTS,
    EAI_PROTOCOL,
    EAI_MAX,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<ErrnoAddressInfo> resolver;

    public final int value() {
        return (int)resolver.longValue(this);
    }

    @Override
    public final int intValue() {
        return (int)resolver.longValue(this);
    }

    @Override
    public final long longValue() {
        return resolver.longValue(this);
    }

    public final String description() {
        return resolver.description(this);
    }

    @Override
    public final boolean defined() {
        return resolver.defined(this);
    }

    public final String toString() {
        return this.description();
    }

    public static ErrnoAddressInfo valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(ErrnoAddressInfo.class, 20000, 29999);
    }
}

