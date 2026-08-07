
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum AddressInfo implements Constant
{
    AI_PASSIVE,
    AI_CANONNAME,
    AI_NUMERICHOST,
    AI_NUMERICSERV,
    AI_MASK,
    AI_ALL,
    AI_V4MAPPED_CFG,
    AI_ADDRCONFIG,
    AI_V4MAPPED,
    AI_DEFAULT,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<AddressInfo> resolver;

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

    public static AddressInfo valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(AddressInfo.class, 20000, 29999);
    }
}

