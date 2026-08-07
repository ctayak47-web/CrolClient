
package jnr.ffi.provider;

import java.nio.charset.Charset;
import jnr.ffi.Address;
import jnr.ffi.Pointer;
import jnr.ffi.provider.AbstractMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;

public final class BoundedMemoryIO
extends AbstractMemoryIO
implements DelegatingMemoryIO {
    private final long base;
    private final long size;
    private final Pointer io;

    public BoundedMemoryIO(Pointer parent, long offset, long size) {
        super(parent.getRuntime(), parent.address() != 0L ? parent.address() + offset : 0L, parent.isDirect());
        this.io = parent;
        this.base = offset;
        this.size = size;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public final boolean hasArray() {
        return this.io.hasArray();
    }

    @Override
    public final Object array() {
        return this.io.array();
    }

    @Override
    public final int arrayOffset() {
        return this.io.arrayOffset() + (int)this.base;
    }

    @Override
    public final int arrayLength() {
        return (int)this.size;
    }

    @Override
    public void checkBounds(long offset, long length) {
        BoundedMemoryIO.checkBounds(this.size, offset, length);
        this.getDelegatedMemoryIO().checkBounds(this.base + offset, length);
    }

    @Override
    public Pointer getDelegatedMemoryIO() {
        return this.io;
    }

    public int hashCode() {
        return this.getDelegatedMemoryIO().hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof BoundedMemoryIO && this.io.equals(((BoundedMemoryIO)obj).io) && ((BoundedMemoryIO)obj).base == this.base && ((BoundedMemoryIO)obj).size == this.size || this.io.equals(obj);
    }

    @Override
    public byte getByte(long offset) {
        BoundedMemoryIO.checkBounds(this.size, offset, 1L);
        return this.io.getByte(this.base + offset);
    }

    @Override
    public short getShort(long offset) {
        BoundedMemoryIO.checkBounds(this.size, offset, 2L);
        return this.io.getShort(this.base + offset);
    }

    @Override
    public int getInt(long offset) {
        BoundedMemoryIO.checkBounds(this.size, offset, 4L);
        return this.io.getInt(this.base + offset);
    }

    @Override
    public long getLongLong(long offset) {
        BoundedMemoryIO.checkBounds(this.size, offset, 8L);
        return this.io.getLongLong(this.base + offset);
    }

    @Override
    public float getFloat(long offset) {
        BoundedMemoryIO.checkBounds(this.size, offset, 4L);
        return this.io.getFloat(this.base + offset);
    }

    @Override
    public double getDouble(long offset) {
        BoundedMemoryIO.checkBounds(this.size, offset, 8L);
        return this.io.getDouble(this.base + offset);
    }

    @Override
    public Pointer getPointer(long offset) {
        BoundedMemoryIO.checkBounds(this.size, offset, this.getRuntime().addressSize());
        return this.io.getPointer(this.base + offset);
    }

    @Override
    public Pointer getPointer(long offset, long size) {
        BoundedMemoryIO.checkBounds(this.size, this.base + offset, this.getRuntime().addressSize());
        return this.io.getPointer(this.base + offset, size);
    }

    @Override
    public void putByte(long offset, byte value) {
        BoundedMemoryIO.checkBounds(this.size, offset, 1L);
        this.io.putByte(this.base + offset, value);
    }

    @Override
    public void putShort(long offset, short value) {
        BoundedMemoryIO.checkBounds(this.size, offset, 2L);
        this.io.putShort(this.base + offset, value);
    }

    @Override
    public void putInt(long offset, int value) {
        BoundedMemoryIO.checkBounds(this.size, offset, 4L);
        this.io.putInt(this.base + offset, value);
    }

    @Override
    public void putLongLong(long offset, long value) {
        BoundedMemoryIO.checkBounds(this.size, offset, 8L);
        this.io.putLongLong(this.base + offset, value);
    }

    @Override
    public void putFloat(long offset, float value) {
        BoundedMemoryIO.checkBounds(this.size, offset, 4L);
        this.io.putFloat(this.base + offset, value);
    }

    @Override
    public void putDouble(long offset, double value) {
        BoundedMemoryIO.checkBounds(this.size, offset, 8L);
        this.io.putDouble(this.base + offset, value);
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        BoundedMemoryIO.checkBounds(this.size, offset, this.getRuntime().addressSize());
        this.io.putPointer(this.base + offset, value);
    }

    @Override
    public void get(long offset, byte[] dst, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, len);
        this.io.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, byte[] dst, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, len);
        this.io.put(this.base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, short[] dst, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 2));
        this.io.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, short[] dst, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 2));
        this.io.put(this.base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, int[] dst, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 4));
        this.io.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, int[] src, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 4));
        this.io.put(this.base + offset, src, off, len);
    }

    @Override
    public void get(long offset, long[] dst, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 8));
        this.io.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, long[] src, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 8));
        this.io.put(this.base + offset, src, off, len);
    }

    @Override
    public void get(long offset, float[] dst, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 4));
        this.io.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, float[] src, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 4));
        this.io.put(this.base + offset, src, off, len);
    }

    @Override
    public void get(long offset, double[] dst, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 8));
        this.io.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, double[] src, int off, int len) {
        BoundedMemoryIO.checkBounds(this.size, offset, Math.multiplyExact(len, 8));
        this.io.put(this.base + offset, src, off, len);
    }

    @Override
    public long getAddress(long offset) {
        BoundedMemoryIO.checkBounds(this.size, offset, this.getRuntime().addressSize());
        return this.io.getAddress(this.base + offset);
    }

    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        BoundedMemoryIO.checkBounds(this.size, offset, maxLength);
        return this.io.getString(this.base + offset, maxLength, cs);
    }

    @Override
    public String getString(long offset) {
        return this.io.getString(this.base + offset, (int)this.size, Charset.defaultCharset());
    }

    @Override
    public void putAddress(long offset, long value) {
        BoundedMemoryIO.checkBounds(this.size, offset, this.getRuntime().addressSize());
        this.io.putAddress(this.base + offset, value);
    }

    @Override
    public void putAddress(long offset, Address value) {
        BoundedMemoryIO.checkBounds(this.size, offset, this.getRuntime().addressSize());
        this.io.putAddress(this.base + offset, value);
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        BoundedMemoryIO.checkBounds(this.size, offset, maxLength);
        this.io.putString(this.base + offset, string, maxLength, cs);
    }

    @Override
    public int indexOf(long offset, byte value) {
        return this.io.indexOf(this.base + offset, value, (int)this.size);
    }

    @Override
    public int indexOf(long offset, byte value, int maxlen) {
        BoundedMemoryIO.checkBounds(this.size, offset, maxlen);
        return this.io.indexOf(this.base + offset, value, maxlen);
    }

    @Override
    public void setMemory(long offset, long size, byte value) {
        BoundedMemoryIO.checkBounds(this.size, this.base + offset, size);
        this.io.setMemory(this.base + offset, size, value);
    }

    @Override
    public void transferFrom(long offset, Pointer other, long otherOffset, long count) {
        BoundedMemoryIO.checkBounds(this.size, this.base + offset, count);
        this.getDelegatedMemoryIO().transferFrom(offset, other, otherOffset, count);
    }

    @Override
    public void transferTo(long offset, Pointer other, long otherOffset, long count) {
        BoundedMemoryIO.checkBounds(this.size, this.base + offset, count);
        this.getDelegatedMemoryIO().transferTo(offset, other, otherOffset, count);
    }
}

