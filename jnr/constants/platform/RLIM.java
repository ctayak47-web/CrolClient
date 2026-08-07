
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum RLIM implements Constant
{
    RLIM_NLIMITS,
    RLIM_INFINITY,
    RLIM_SAVED_MAX,
    RLIM_SAVED_CUR,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<RLIM> resolver;

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

    public static RLIM valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(RLIM.class, 20000, 29999);
    }
}

