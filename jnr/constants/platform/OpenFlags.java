
package jnr.constants.platform;

import jnr.constants.Constant;
import jnr.constants.platform.ConstantResolver;

public enum OpenFlags implements Constant
{
    O_RDONLY,
    O_WRONLY,
    O_RDWR,
    O_ACCMODE,
    O_NONBLOCK,
    O_APPEND,
    O_SYNC,
    O_SHLOCK,
    O_EXLOCK,
    O_ASYNC,
    O_FSYNC,
    O_NOFOLLOW,
    O_CREAT,
    O_TRUNC,
    O_EXCL,
    O_EVTONLY,
    O_DIRECTORY,
    O_SYMLINK,
    O_BINARY,
    O_NOCTTY,
    O_TMPFILE,
    O_CLOEXEC,
    __UNKNOWN_CONSTANT__;

    private static final ConstantResolver<OpenFlags> resolver;

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

    public static OpenFlags valueOf(long value) {
        return resolver.valueOf(value);
    }

    static {
        resolver = ConstantResolver.getBitmaskResolver(OpenFlags.class);
    }
}

