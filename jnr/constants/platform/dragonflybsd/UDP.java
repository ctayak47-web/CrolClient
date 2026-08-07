
package jnr.constants.platform.dragonflybsd;

import jnr.constants.Constant;

public enum UDP implements Constant
{

    private final long value;
    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 0L;

    private UDP(long value) {
        this.value = value;
    }

    public final int value() {
        return (int)this.value;
    }

    @Override
    public final int intValue() {
        return (int)this.value;
    }

    @Override
    public final long longValue() {
        return this.value;
    }

    @Override
    public final boolean defined() {
        return true;
    }
}

