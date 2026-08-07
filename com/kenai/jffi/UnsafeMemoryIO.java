
package com.kenai.jffi;

import com.kenai.jffi.Foreign;
import com.kenai.jffi.MemoryIO;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

public abstract class UnsafeMemoryIO
extends MemoryIO {
    protected static Unsafe unsafe = (Unsafe)Unsafe.class.cast(UnsafeMemoryIO.getUnsafe());

    private static Object getUnsafe() {
        try {
            Class<?> sunUnsafe = Class.forName("sun.misc.Unsafe");
            Field f = sunUnsafe.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return f.get(sunUnsafe);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public final byte getByte(long address) {
        return unsafe.getByte(address);
    }

    @Override
    public final short getShort(long address) {
        return unsafe.getShort(address);
    }

    @Override
    public final int getInt(long address) {
        return unsafe.getInt(address);
    }

    @Override
    public final long getLong(long address) {
        return unsafe.getLong(address);
    }

    @Override
    public final float getFloat(long address) {
        return unsafe.getFloat(address);
    }

    @Override
    public final double getDouble(long address) {
        return unsafe.getDouble(address);
    }

    @Override
    public final void putByte(long address, byte value) {
        unsafe.putByte(address, value);
    }

    @Override
    public final void putShort(long address, short value) {
        unsafe.putShort(address, value);
    }

    @Override
    public final void putInt(long address, int value) {
        unsafe.putInt(address, value);
    }

    @Override
    public final void putLong(long address, long value) {
        unsafe.putLong(address, value);
    }

    @Override
    public final void putFloat(long address, float value) {
        unsafe.putFloat(address, value);
    }

    @Override
    public final void putDouble(long address, double value) {
        unsafe.putDouble(address, value);
    }

    @Override
    public final void _copyMemory(long src, long dst, long size) {
        unsafe.copyMemory(src, dst, size);
    }

    @Override
    public final void setMemory(long src, long size, byte value) {
        unsafe.setMemory(src, size, value);
    }

    @Override
    public final void memcpy(long dst, long src, long size) {
        Foreign.memcpy(dst, src, size);
    }

    @Override
    public final void memmove(long dst, long src, long size) {
        Foreign.memmove(dst, src, size);
    }

    @Override
    public final long memchr(long address, int value, long size) {
        return Foreign.memchr(address, value, size);
    }

    @Override
    public final void putByteArray(long address, byte[] data, int offset, int length) {
        Foreign.putByteArray(address, data, offset, length);
    }

    @Override
    public final void getByteArray(long address, byte[] data, int offset, int length) {
        Foreign.getByteArray(address, data, offset, length);
    }

    @Override
    public final void putCharArray(long address, char[] data, int offset, int length) {
        Foreign.putCharArray(address, data, offset, length);
    }

    @Override
    public final void getCharArray(long address, char[] data, int offset, int length) {
        Foreign.getCharArray(address, data, offset, length);
    }

    @Override
    public final void putShortArray(long address, short[] data, int offset, int length) {
        Foreign.putShortArray(address, data, offset, length);
    }

    @Override
    public final void getShortArray(long address, short[] data, int offset, int length) {
        Foreign.getShortArray(address, data, offset, length);
    }

    @Override
    public final void putIntArray(long address, int[] data, int offset, int length) {
        Foreign.putIntArray(address, data, offset, length);
    }

    @Override
    public final void getIntArray(long address, int[] data, int offset, int length) {
        Foreign.getIntArray(address, data, offset, length);
    }

    @Override
    public final void putLongArray(long address, long[] data, int offset, int length) {
        Foreign.putLongArray(address, data, offset, length);
    }

    @Override
    public final void getLongArray(long address, long[] data, int offset, int length) {
        Foreign.getLongArray(address, data, offset, length);
    }

    @Override
    public final void putFloatArray(long address, float[] data, int offset, int length) {
        Foreign.putFloatArray(address, data, offset, length);
    }

    @Override
    public final void getFloatArray(long address, float[] data, int offset, int length) {
        Foreign.getFloatArray(address, data, offset, length);
    }

    @Override
    public final void putDoubleArray(long address, double[] data, int offset, int length) {
        Foreign.putDoubleArray(address, data, offset, length);
    }

    @Override
    public final void getDoubleArray(long address, double[] data, int offset, int length) {
        Foreign.getDoubleArray(address, data, offset, length);
    }

    @Override
    public final long getStringLength(long address) {
        return Foreign.strlen(address);
    }

    @Override
    public final byte[] getZeroTerminatedByteArray(long address) {
        return Foreign.getZeroTerminatedByteArray(address);
    }

    @Override
    public final byte[] getZeroTerminatedByteArray(long address, int maxlen) {
        return Foreign.getZeroTerminatedByteArray(address, maxlen);
    }

    @Override
    public final void putZeroTerminatedByteArray(long address, byte[] data, int offset, int length) {
        Foreign.putZeroTerminatedByteArray(address, data, offset, length);
    }

    static class UnsafeMemoryIO64
    extends UnsafeMemoryIO {
        UnsafeMemoryIO64() {
        }

        @Override
        public final long getAddress(long address) {
            return unsafe.getLong(address);
        }

        @Override
        public final void putAddress(long address, long value) {
            unsafe.putLong(address, value);
        }
    }

    static class UnsafeMemoryIO32
    extends UnsafeMemoryIO {
        UnsafeMemoryIO32() {
        }

        @Override
        public final long getAddress(long address) {
            return (long)unsafe.getInt(address) & ADDRESS_MASK;
        }

        @Override
        public final void putAddress(long address, long value) {
            unsafe.putInt(address, (int)value);
        }
    }
}

