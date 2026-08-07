
package jnr.ffi.provider;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import jnr.ffi.Runtime;
import jnr.ffi.provider.AbstractMemoryIO;
import jnr.ffi.util.BufferUtil;

public abstract class AbstractBufferMemoryIO
extends AbstractMemoryIO {
    protected final ByteBuffer buffer;

    public AbstractBufferMemoryIO(Runtime runtime, ByteBuffer buffer, long address) {
        super(runtime, address, buffer.isDirect());
        this.buffer = buffer;
    }

    @Override
    public long size() {
        return this.buffer.remaining();
    }

    public final ByteBuffer getByteBuffer() {
        return this.buffer;
    }

    @Override
    public int arrayLength() {
        return this.getByteBuffer().remaining();
    }

    @Override
    public int arrayOffset() {
        return this.getByteBuffer().arrayOffset();
    }

    @Override
    public Object array() {
        return this.getByteBuffer().array();
    }

    @Override
    public boolean hasArray() {
        return this.getByteBuffer().hasArray();
    }

    @Override
    public byte getByte(long offset) {
        return this.buffer.get((int)offset);
    }

    @Override
    public short getShort(long offset) {
        return this.buffer.getShort((int)offset);
    }

    @Override
    public int getInt(long offset) {
        return this.buffer.getInt((int)offset);
    }

    @Override
    public long getLongLong(long offset) {
        return this.buffer.getLong((int)offset);
    }

    @Override
    public float getFloat(long offset) {
        return this.buffer.getFloat((int)offset);
    }

    @Override
    public double getDouble(long offset) {
        return this.buffer.getDouble((int)offset);
    }

    @Override
    public void putByte(long offset, byte value) {
        this.buffer.put((int)offset, value);
    }

    @Override
    public void putShort(long offset, short value) {
        this.buffer.putShort((int)offset, value);
    }

    @Override
    public void putInt(long offset, int value) {
        this.buffer.putInt((int)offset, value);
    }

    @Override
    public void putLongLong(long offset, long value) {
        this.buffer.putLong((int)offset, value);
    }

    @Override
    public void putFloat(long offset, float value) {
        this.buffer.putFloat((int)offset, value);
    }

    @Override
    public void putDouble(long offset, double value) {
        this.buffer.putDouble((int)offset, value);
    }

    public String getString(long offset, int size) {
        return BufferUtil.getString(BufferUtil.slice(this.buffer, (int)offset), Charset.defaultCharset());
    }

    public void putString(long offset, String string) {
        BufferUtil.putString(BufferUtil.slice(this.buffer, (int)offset), Charset.defaultCharset(), string);
    }

    @Override
    public void get(long offset, byte[] dst, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len).get(dst, off, len);
    }

    @Override
    public void get(long offset, short[] dst, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 16 / 8).asShortBuffer().get(dst, off, len);
    }

    @Override
    public void get(long offset, int[] dst, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 32 / 8).asIntBuffer().get(dst, off, len);
    }

    @Override
    public void get(long offset, long[] dst, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 64 / 8).asLongBuffer().get(dst, off, len);
    }

    @Override
    public void get(long offset, float[] dst, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 32 / 8).asFloatBuffer().get(dst, off, len);
    }

    @Override
    public void get(long offset, double[] dst, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 64 / 8).asDoubleBuffer().get(dst, off, len);
    }

    @Override
    public void put(long offset, byte[] dst, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len).put(dst, off, len);
    }

    @Override
    public void put(long offset, short[] dst, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 16 / 8).asShortBuffer().put(dst, off, len);
    }

    @Override
    public void put(long offset, int[] src, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 32 / 8).asIntBuffer().put(src, off, len);
    }

    @Override
    public void put(long offset, long[] src, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 64 / 8).asLongBuffer().put(src, off, len);
    }

    @Override
    public void put(long offset, float[] src, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 32 / 8).asFloatBuffer().put(src, off, len);
    }

    @Override
    public void put(long offset, double[] src, int off, int len) {
        BufferUtil.slice(this.buffer, (int)offset, len * 64 / 8).asDoubleBuffer().put(src, off, len);
    }

    @Override
    public String getString(long offset) {
        return BufferUtil.getString(BufferUtil.slice(this.buffer, (int)offset), Charset.defaultCharset());
    }

    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        return BufferUtil.getString(BufferUtil.slice(this.buffer, (int)offset, maxLength), cs);
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        BufferUtil.putString(BufferUtil.slice(this.buffer, (int)offset, maxLength), cs, string);
    }

    @Override
    public int indexOf(long offset, byte value, int maxlen) {
        while (offset > -1L) {
            if (this.buffer.get((int)offset) == value) {
                return (int)offset;
            }
            ++offset;
        }
        return -1;
    }

    @Override
    public void setMemory(long offset, long size, byte value) {
        int i = 0;
        while ((long)i < size) {
            this.buffer.put((int)offset + i, value);
            ++i;
        }
    }
}

