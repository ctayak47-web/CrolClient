
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum Sock implements Constant
{
    SOCK_STREAM,
    SOCK_DGRAM,
    SOCK_RAW,
    SOCK_RDM,
    SOCK_SEQPACKET,
    SOCK_NONBLOCK,
    SOCK_CLOEXEC,
    SOCK_MAXADDRLEN,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<Sock> resolver;

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

    public static Sock valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(Sock.class, 20000, 29999);
    }
}

