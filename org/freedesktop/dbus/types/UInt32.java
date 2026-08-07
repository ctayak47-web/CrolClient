
package org.freedesktop.dbus.types;

public class UInt32
extends Number
implements Comparable<UInt32> {
    public static final long MAX_VALUE = 0xFFFFFFFFL;
    public static final long MIN_VALUE = 0L;
    private final long value;

    public UInt32(long _value) {
        if (_value < 0L || _value > 0xFFFFFFFFL) {
            throw new NumberFormatException(String.format("%s is not between %s and %s.", _value, 0L, 0xFFFFFFFFL));
        }
        this.value = _value;
    }

    public UInt32(String _value) {
        this(Long.parseLong(_value));
    }

    @Override
    public byte byteValue() {
        return (byte)this.value;
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return (int)this.value;
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public short shortValue() {
        return (short)this.value;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object _o) {
        if (!(_o instanceof UInt32)) return false;
        UInt32 ui = (UInt32)_o;
        if (ui.value != this.value) return false;
        return true;
    }

    public int hashCode() {
        return (int)this.value;
    }

    @Override
    public int compareTo(UInt32 _other) {
        return Long.compare(this.value, _other.value);
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}

