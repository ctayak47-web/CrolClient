
package jnr.ffi.provider;

import java.nio.charset.Charset;
import jnr.ffi.Pointer;
import jnr.ffi.provider.AbstractMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;

public class ShareMemoryIO
extends AbstractMemoryIO
implements DelegatingMemoryIO {
    private final Pointer ptr;
    private final long base;

    public ShareMemoryIO(Pointer parent, long offset) {
        super(parent.getRuntime(), parent.address() != 0L ? parent.address() + offset : 0L, parent.isDirect());
        this.ptr = parent;
        this.base = offset;
    }

    @Override
    public long size() {
        return this.ptr.size() - this.base;
    }

    @Override
    public final boolean hasArray() {
        return this.ptr.hasArray();
    }

    @Override
    public final Object array() {
        return this.ptr.array();
    }

    @Override
    public final int arrayOffset() {
        return this.ptr.arrayOffset() + (int)this.base;
    }

    @Override
    public final int arrayLength() {
        return this.ptr.arrayLength() - (int)this.base;
    }

    @Override
    public final Pointer getDelegatedMemoryIO() {
        return this.ptr;
    }

    @Override
    public byte getByte(long offset) {
        return this.ptr.getByte(this.base + offset);
    }

    @Override
    public short getShort(long offset) {
        return this.ptr.getShort(this.base + offset);
    }

    @Override
    public int getInt(long offset) {
        return this.ptr.getInt(this.base + offset);
    }

    @Override
    public long getLong(long offset) {
        return this.ptr.getLong(this.base + offset);
    }

    @Override
    public long getLongLong(long offset) {
        return this.ptr.getLongLong(this.base + offset);
    }

    @Override
    public float getFloat(long offset) {
        return this.ptr.getFloat(this.base + offset);
    }

    @Override
    public double getDouble(long offset) {
        return this.ptr.getDouble(this.base + offset);
    }

    @Override
    public Pointer getPointer(long offset) {
        return this.ptr.getPointer(this.base + offset);
    }

    @Override
    public Pointer getPointer(long offset, long size) {
        return this.ptr.getPointer(this.base + offset, size);
    }

    @Override
    public String getString(long offset) {
        return this.ptr.getString(this.base + offset);
    }

    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        return this.ptr.getString(this.base + offset, maxLength, cs);
    }

    @Override
    public void putByte(long offset, byte value) {
        this.ptr.putByte(this.base + offset, value);
    }

    @Override
    public void putShort(long offset, short value) {
        this.ptr.putShort(this.base + offset, value);
    }

    @Override
    public void putInt(long offset, int value) {
        this.ptr.putInt(this.base + offset, value);
    }

    @Override
    public void putLong(long offset, long value) {
        this.ptr.putLong(this.base + offset, value);
    }

    @Override
    public void putLongLong(long offset, long value) {
        this.ptr.putLongLong(this.base + offset, value);
    }

    @Override
    public void putFloat(long offset, float value) {
        this.ptr.putFloat(this.base + offset, value);
    }

    @Override
    public void putDouble(long offset, double value) {
        this.ptr.putDouble(this.base + offset, value);
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        this.ptr.putPointer(this.base + offset, value);
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        this.ptr.putString(this.base + offset, string, maxLength, cs);
    }

    @Override
    public void get(long offset, byte[] dst, int off, int len) {
        this.ptr.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, byte[] dst, int off, int len) {
        this.ptr.put(this.base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, short[] dst, int off, int len) {
        this.ptr.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, short[] dst, int off, int len) {
        this.ptr.put(this.base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, int[] dst, int off, int len) {
        this.ptr.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, int[] src, int off, int len) {
        this.ptr.put(this.base + offset, src, off, len);
    }

    @Override
    public void get(long offset, long[] dst, int off, int len) {
        this.ptr.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, long[] src, int off, int len) {
        this.ptr.put(this.base + offset, src, off, len);
    }

    @Override
    public void get(long offset, float[] dst, int off, int len) {
        this.ptr.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, float[] src, int off, int len) {
        this.ptr.put(this.base + offset, src, off, len);
    }

    @Override
    public void get(long offset, double[] dst, int off, int len) {
        this.ptr.get(this.base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, double[] src, int off, int len) {
        this.ptr.put(this.base + offset, src, off, len);
    }

    @Override
    public int indexOf(long offset, byte value, int maxlen) {
        return this.ptr.indexOf(this.base + offset, value, maxlen);
    }

    @Override
    public void setMemory(long offset, long size, byte value) {
        this.ptr.setMemory(this.base + offset, size, value);
    }
}

