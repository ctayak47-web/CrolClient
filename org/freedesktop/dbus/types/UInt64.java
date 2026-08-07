
package org.freedesktop.dbus.types;

import java.math.BigInteger;

public class UInt64
extends Number
implements Comparable<UInt64> {
    public static final long MAX_LONG_VALUE = Long.MAX_VALUE;
    public static final BigInteger MAX_BIG_VALUE = new BigInteger("18446744073709551615");
    public static final long MIN_VALUE = 0L;
    private static final String BOUNDS = "4294967295";
    private static final String ERROR_MSG = "%s is not between %s and %s.";
    private final BigInteger value;
    private final long top;
    private final long bottom;

    public UInt64(long _value) {
        if (_value < 0L || _value > Long.MAX_VALUE) {
            throw new NumberFormatException(String.format(ERROR_MSG, _value, 0L, Long.MAX_VALUE));
        }
        this.value = BigInteger.valueOf(_value);
        this.top = this.value.shiftRight(32).and(new BigInteger(BOUNDS)).longValue();
        this.bottom = this.value.and(new BigInteger(BOUNDS)).longValue();
    }

    public UInt64(long _top, long _bottom) {
        BigInteger a = BigInteger.valueOf(_top);
        a = a.shiftLeft(32);
        a = a.add(BigInteger.valueOf(_bottom));
        if (0 > a.compareTo(BigInteger.ZERO)) {
            throw new NumberFormatException(String.format(ERROR_MSG, a, 0L, MAX_BIG_VALUE));
        }
        if (0 < a.compareTo(MAX_BIG_VALUE)) {
            throw new NumberFormatException(String.format(ERROR_MSG, a, 0L, MAX_BIG_VALUE));
        }
        this.value = a;
        this.top = _top;
        this.bottom = _bottom;
    }

    public UInt64(BigInteger _value) {
        if (null == _value || 0 > _value.compareTo(BigInteger.ZERO) || 0 < _value.compareTo(MAX_BIG_VALUE)) {
            throw new NumberFormatException(String.format(ERROR_MSG, _value, 0L, MAX_BIG_VALUE));
        }
        this.value = _value;
        this.top = this.value.shiftRight(32).and(new BigInteger(BOUNDS)).longValue();
        this.bottom = this.value.and(new BigInteger(BOUNDS)).longValue();
    }

    public UInt64(String _value) {
        if (null == _value) {
            throw new NumberFormatException(String.format(ERROR_MSG, _value, 0L, MAX_BIG_VALUE));
        }
        BigInteger a = new BigInteger(_value);
        if (0 > a.compareTo(BigInteger.ZERO) || 0 < a.compareTo(MAX_BIG_VALUE)) {
            throw new NumberFormatException(String.format(ERROR_MSG, _value, 0L, MAX_BIG_VALUE));
        }
        this.value = a;
        this.top = this.value.shiftRight(32).and(new BigInteger(BOUNDS)).longValue();
        this.bottom = this.value.and(new BigInteger(BOUNDS)).longValue();
    }

    public BigInteger value() {
        return this.value;
    }

    @Override
    public byte byteValue() {
        return this.value.byteValue();
    }

    @Override
    public double doubleValue() {
        return this.value.doubleValue();
    }

    @Override
    public float floatValue() {
        return this.value.floatValue();
    }

    @Override
    public int intValue() {
        return this.value.intValue();
    }

    @Override
    public long longValue() {
        return this.value.longValue();
    }

    @Override
    public short shortValue() {
        return this.value.shortValue();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object _o) {
        if (!(_o instanceof UInt64)) return false;
        UInt64 ui = (UInt64)_o;
        if (!this.value.equals(ui.value)) return false;
        return true;
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public int compareTo(UInt64 _other) {
        return this.value.compareTo(_other.value);
    }

    public String toString() {
        return this.value.toString();
    }

    public long top() {
        return this.top;
    }

    public long bottom() {
        return this.bottom;
    }
}

