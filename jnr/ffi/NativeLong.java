
package jnr.ffi;

public final class NativeLong
extends Number
implements Comparable<NativeLong> {
    private static final NativeLong ZERO = new NativeLong(0);
    private static final NativeLong ONE = new NativeLong(1);
    private static final NativeLong MINUS_ONE = new NativeLong(-1);
    private final long value;

    public NativeLong(long value) {
        this.value = value;
    }

    public NativeLong(int value) {
        this.value = value;
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
    public final float floatValue() {
        return this.value;
    }

    @Override
    public final double doubleValue() {
        return this.value;
    }

    public final int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }

    public final boolean equals(Object obj) {
        return obj instanceof NativeLong && this.value == ((NativeLong)obj).value;
    }

    public String toString() {
        return String.valueOf(this.value);
    }

    @Override
    public final int compareTo(NativeLong other) {
        return this.value < other.value ? -1 : (this.value > other.value ? 1 : 0);
    }

    private static NativeLong _valueOf(long value) {
        return value >= -128L && value <= 127L ? Cache.cache[128 + (int)value] : new NativeLong(value);
    }

    private static NativeLong _valueOf(int value) {
        return value >= -128 && value <= 127 ? Cache.cache[128 + value] : new NativeLong(value);
    }

    public static NativeLong valueOf(long value) {
        return value == 0L ? ZERO : (value == 1L ? ONE : (value == -1L ? MINUS_ONE : NativeLong._valueOf(value)));
    }

    public static NativeLong valueOf(int value) {
        return value == 0 ? ZERO : (value == 1 ? ONE : (value == -1 ? MINUS_ONE : NativeLong._valueOf(value)));
    }

    private static final class Cache {
        static final NativeLong[] cache = new NativeLong[256];

        private Cache() {
        }

        static {
            for (int i = 0; i < cache.length; ++i) {
                Cache.cache[i] = new NativeLong(i - 128);
            }
            Cache.cache[128] = ZERO;
            Cache.cache[129] = ONE;
            Cache.cache[127] = MINUS_ONE;
        }
    }
}

