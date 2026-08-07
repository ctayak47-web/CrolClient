
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum Locale implements Constant
{
    LC_CTYPE,
    LC_NUMERIC,
    LC_TIME,
    LC_COLLATE,
    LC_MONETARY,
    LC_MESSAGES,
    LC_ALL,
    LC_PAPER,
    LC_NAME,
    LC_ADDRESS,
    LC_TELEPHONE,
    LC_MEASUREMENT,
    LC_IDENTIFICATION,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<Locale> resolver;

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

    public static Locale valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(Locale.class, 20000, 29999);
    }
}

