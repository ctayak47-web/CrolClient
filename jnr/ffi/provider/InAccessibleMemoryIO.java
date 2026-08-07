
package jnr.ffi.provider;

import java.nio.charset.Charset;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.provider.AbstractMemoryIO;

public abstract class InAccessibleMemoryIO
extends AbstractMemoryIO {
    private static final String msg = "attempted access to inaccessible memory";

    protected InAccessibleMemoryIO(Runtime runtime, long address, boolean isDirect) {
        super(runtime, address, isDirect);
    }

    protected RuntimeException error() {
        return new IndexOutOfBoundsException(msg);
    }

    @Override
    public boolean hasArray() {
        return false;
    }

    @Override
    public Object array() {
        return null;
    }

    @Override
    public int arrayOffset() {
        return 0;
    }

    @Override
    public int arrayLength() {
        return 0;
    }

    @Override
    public final byte getByte(long offset) {
        throw this.error();
    }

    @Override
    public final short getShort(long offset) {
        throw this.error();
    }

    @Override
    public final int getInt(long offset) {
        throw this.error();
    }

    @Override
    public final long getLong(long offset) {
        throw this.error();
    }

    @Override
    public final long getLongLong(long offset) {
        throw this.error();
    }

    @Override
    public final float getFloat(long offset) {
        throw this.error();
    }

    @Override
    public final double getDouble(long offset) {
        throw this.error();
    }

    @Override
    public final void putByte(long offset, byte value) {
        throw this.error();
    }

    @Override
    public final void putShort(long offset, short value) {
        throw this.error();
    }

    @Override
    public final void putInt(long offset, int value) {
        throw this.error();
    }

    @Override
    public final void putLong(long offset, long value) {
        throw this.error();
    }

    @Override
    public final void putLongLong(long offset, long value) {
        throw this.error();
    }

    @Override
    public final void putFloat(long offset, float value) {
        throw this.error();
    }

    @Override
    public final void putDouble(long offset, double value) {
        throw this.error();
    }

    @Override
    public final void get(long offset, byte[] dst, int off, int len) {
        throw this.error();
    }

    @Override
    public final void put(long offset, byte[] dst, int off, int len) {
        throw this.error();
    }

    @Override
    public final void get(long offset, short[] dst, int off, int len) {
        throw this.error();
    }

    @Override
    public final void put(long offset, short[] dst, int off, int len) {
        throw this.error();
    }

    @Override
    public final void get(long offset, int[] dst, int off, int len) {
        throw this.error();
    }

    @Override
    public final void put(long offset, int[] src, int off, int len) {
        throw this.error();
    }

    @Override
    public final void get(long offset, long[] dst, int off, int len) {
        throw this.error();
    }

    @Override
    public final void put(long offset, long[] src, int off, int len) {
        throw this.error();
    }

    @Override
    public final void get(long offset, float[] dst, int off, int len) {
        throw this.error();
    }

    @Override
    public final void put(long offset, float[] src, int off, int len) {
        throw this.error();
    }

    @Override
    public final void get(long offset, double[] dst, int off, int len) {
        throw this.error();
    }

    @Override
    public final void put(long offset, double[] src, int off, int len) {
        throw this.error();
    }

    @Override
    public final Pointer getPointer(long offset, long size) {
        throw this.error();
    }

    @Override
    public final Pointer getPointer(long offset) {
        throw this.error();
    }

    @Override
    public final void putPointer(long offset, Pointer value) {
        throw this.error();
    }

    @Override
    public String getString(long offset) {
        throw this.error();
    }

    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        throw this.error();
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        throw this.error();
    }

    @Override
    public final int indexOf(long offset, byte value, int maxlen) {
        throw this.error();
    }

    @Override
    public final void setMemory(long offset, long size, byte value) {
        throw this.error();
    }
}

