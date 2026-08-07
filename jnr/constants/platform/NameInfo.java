
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum NameInfo implements Constant
{
    NI_MAXHOST,
    NI_MAXSERV,
    NI_NOFQDN,
    NI_NUMERICHOST,
    NI_NAMEREQD,
    NI_NUMERICSERV,
    NI_DGRAM,
    NI_WITHSCOPEID,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<NameInfo> resolver;

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

    public static NameInfo valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(NameInfo.class, 20000, 29999);
    }
}

