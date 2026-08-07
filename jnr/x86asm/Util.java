
package jnr.x86asm;

public final class Util {
    private Util() {
    }

    static final boolean isInt8(long x) {
        return x >= -128L && x <= 127L;
    }

    static final boolean isUInt8(long x) {
        return x >= 0L && x <= 255L;
    }

    static final boolean isInt16(long x) {
        return x >= -32768L && x <= 32767L;
    }

    static final boolean isUInt16(long x) {
        return x >= 0L && x <= 65535L;
    }

    static final boolean isInt32(long x) {
        return x >= Integer.MIN_VALUE && x <= Integer.MAX_VALUE;
    }

    static final boolean isUInt32(long x) {
        return x >= 0L && x <= 0xFFFFFFFFL;
    }
}

