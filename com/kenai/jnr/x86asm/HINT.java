
package com.kenai.jnr.x86asm;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
public enum HINT {
    HINT_NONE(0),
    HINT_TAKEN(62),
    HINT_NOT_TAKEN(46);

    private final int value;

    private HINT(int value) {
        this.value = value;
    }

    public final int value() {
        return this.value;
    }

    public static final HINT valueOf(int value) {
        switch (value) {
            case 62: {
                return HINT_TAKEN;
            }
            case 46: {
                return HINT_NOT_TAKEN;
            }
        }
        return HINT_NONE;
    }
}

