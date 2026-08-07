
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum SocketControlMessage implements Constant
{
    SCM_RIGHTS,
    SCM_TIMESTAMP,
    SCM_TIMESTAMPNS,
    SCM_TIMESTAMPING,
    SCM_BINTIME,
    SCM_CREDENTIALS,
    SCM_CREDS,
    SCM_UCRED,
    SCM_WIFI_STATUS,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<SocketControlMessage> resolver;

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

    public static SocketControlMessage valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(SocketControlMessage.class, 20000, 29999);
    }
}

