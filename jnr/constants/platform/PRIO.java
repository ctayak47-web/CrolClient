
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum PRIO implements Constant
{
    PRIO_MIN,
    PRIO_PROCESS,
    PRIO_PGRP,
    PRIO_USER,
    PRIO_MAX,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<PRIO> resolver;

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

    public static PRIO valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(PRIO.class, 20000, 29999);
    }
}

