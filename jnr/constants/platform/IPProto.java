
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum IPProto implements Constant
{
    IPPROTO_IP,
    IPPROTO_HOPOPTS,
    IPPROTO_ICMP,
    IPPROTO_IGMP,
    IPPROTO_IPIP,
    IPPROTO_TCP,
    IPPROTO_EGP,
    IPPROTO_PUP,
    IPPROTO_UDP,
    IPPROTO_IDP,
    IPPROTO_TP,
    IPPROTO_IPV6,
    IPPROTO_ROUTING,
    IPPROTO_FRAGMENT,
    IPPROTO_RSVP,
    IPPROTO_GRE,
    IPPROTO_ESP,
    IPPROTO_AH,
    IPPROTO_ICMPV6,
    IPPROTO_NONE,
    IPPROTO_DSTOPTS,
    IPPROTO_MTP,
    IPPROTO_ENCAP,
    IPPROTO_PIM,
    IPPROTO_COMP,
    IPPROTO_SCTP,
    IPPROTO_RAW,
    IPPROTO_MAX,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<IPProto> resolver;

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

    public static IPProto valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getResolver(IPProto.class, 20000, 29999);
    }
}

