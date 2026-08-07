
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum Multicast implements Constant
{
    MCAST_JOIN_GROUP,
    MCAST_BLOCK_SOURCE,
    MCAST_UNBLOCK_SOURCE,
    MCAST_LEAVE_GROUP,
    MCAST_JOIN_SOURCE_GROUP,
    MCAST_LEAVE_SOURCE_GROUP,
    MCAST_MSFILTER,
    MCAST_EXCLUDE,
    MCAST_INCLUDE,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<Multicast> resolver;

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

    public static Multicast valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(Multicast.class, 20000, 29999);
    }
}

