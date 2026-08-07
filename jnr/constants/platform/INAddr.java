
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum INAddr implements Constant
{
    INADDR_ANY,
    INADDR_BROADCAST,
    INADDR_NONE,
    INADDR_LOOPBACK,
    INADDR_UNSPEC_GROUP,
    INADDR_ALLHOSTS_GROUP,
    INADDR_ALLRTRS_GROUP,
    INADDR_MAX_LOCAL_GROUP,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<INAddr> resolver;

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

    public static INAddr valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(INAddr.class, 20000, 29999);
    }
}

