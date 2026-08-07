
package com.kenai.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Foreign;
import com.kenai.jffi.Function;
import com.kenai.jffi.InvocationBuffer;
import com.kenai.jffi.ObjectBuffer;
import com.kenai.jffi.ObjectParameterInfo;
import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import java.math.BigDecimal;
import java.nio.Buffer;
import java.nio.ByteOrder;

public final class HeapInvocationBuffer
extends InvocationBuffer {
    private static final int PARAM_SIZE = 8;
    private final CallContext callContext;
    private final byte[] buffer;
    private ObjectBuffer objectBuffer;
    private int paramOffset = 0;
    private int paramIndex = 0;

    public HeapInvocationBuffer(Function function) {
        this.callContext = function.getCallContext();
        this.buffer = new byte[Encoder.getInstance().getBufferSize(this.callContext)];
    }

    public HeapInvocationBuffer(CallContext callContext) {
        this.callContext = callContext;
        this.buffer = new byte[Encoder.getInstance().getBufferSize(callContext)];
    }

    public HeapInvocationBuffer(CallContext context, int objectCount) {
        this.callContext = context;
        this.buffer = new byte[Encoder.getInstance().getBufferSize(context)];
        this.objectBuffer = new ObjectBuffer(objectCount);
    }

    byte[] array() {
        return this.buffer;
    }

    ObjectBuffer objectBuffer() {
        return this.objectBuffer;
    }

    @Override
    public final void putByte(int value) {
        this.paramOffset = Encoder.getInstance().putByte(this.buffer, this.paramOffset, value);
        ++this.paramIndex;
    }

    @Override
    public final void putShort(int value) {
        this.paramOffset = Encoder.getInstance().putShort(this.buffer, this.paramOffset, value);
        ++this.paramIndex;
    }

    @Override
    public final void putInt(int value) {
        this.paramOffset = Encoder.getInstance().putInt(this.buffer, this.paramOffset, value);
        ++this.paramIndex;
    }

    @Override
    public final void putLong(long value) {
        this.paramOffset = Encoder.getInstance().putLong(this.buffer, this.paramOffset, value);
        ++this.paramIndex;
    }

    @Override
    public final void putFloat(float value) {
        this.paramOffset = Encoder.getInstance().putFloat(this.buffer, this.paramOffset, value);
        ++this.paramIndex;
    }

    @Override
    public final void putDouble(double value) {
        this.paramOffset = Encoder.getInstance().putDouble(this.buffer, this.paramOffset, value);
        ++this.paramIndex;
    }

    public final void putLongDouble(double value) {
        byte[] ld = new byte[Type.LONGDOUBLE.size()];
        Foreign.getInstance().longDoubleFromDouble(value, ld, 0, Type.LONGDOUBLE.size());
        this.getObjectBuffer().putArray(this.paramIndex, ld, 0, ld.length, 1);
        this.paramOffset += 8;
        ++this.paramIndex;
    }

    public final void putLongDouble(BigDecimal value) {
        byte[] ld = new byte[Type.LONGDOUBLE.size()];
        Foreign.getInstance().longDoubleFromString(value.toEngineeringString(), ld, 0, Type.LONGDOUBLE.size());
        this.getObjectBuffer().putArray(this.paramIndex, ld, 0, ld.length, 1);
        this.paramOffset += 8;
        ++this.paramIndex;
    }

    @Override
    public final void putAddress(long value) {
        this.paramOffset = Encoder.getInstance().putAddress(this.buffer, this.paramOffset, value);
        ++this.paramIndex;
    }

    private final ObjectBuffer getObjectBuffer() {
        if (this.objectBuffer == null) {
            this.objectBuffer = new ObjectBuffer();
        }
        return this.objectBuffer;
    }

    @Override
    public final void putArray(byte[] array, int offset, int length, int flags) {
        this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
        this.getObjectBuffer().putArray(this.paramIndex++, array, offset, length, flags);
    }

    @Override
    public final void putArray(short[] array, int offset, int length, int flags) {
        this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
        this.getObjectBuffer().putArray(this.paramIndex++, array, offset, length, flags);
    }

    @Override
    public final void putArray(int[] array, int offset, int length, int flags) {
        this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
        this.getObjectBuffer().putArray(this.paramIndex++, array, offset, length, flags);
    }

    @Override
    public final void putArray(long[] array, int offset, int length, int flags) {
        this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
        this.getObjectBuffer().putArray(this.paramIndex++, array, offset, length, flags);
    }

    @Override
    public final void putArray(float[] array, int offset, int length, int flags) {
        this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
        this.getObjectBuffer().putArray(this.paramIndex++, array, offset, length, flags);
    }

    @Override
    public final void putArray(double[] array, int offset, int length, int flags) {
        this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
        this.getObjectBuffer().putArray(this.paramIndex++, array, offset, length, flags);
    }

    @Override
    public final void putDirectBuffer(Buffer value, int offset, int length) {
        this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
        this.getObjectBuffer().putDirectBuffer(this.paramIndex++, value, offset, length);
    }

    @Override
    public final void putStruct(byte[] struct, int offset) {
        Type type = this.callContext.getParameterType(this.paramIndex);
        this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
        this.getObjectBuffer().putArray(this.paramIndex, struct, offset, type.size(), 1);
        ++this.paramIndex;
    }

    @Override
    public final void putStruct(long struct) {
        Type type = this.callContext.getParameterType(this.paramIndex);
        this.paramOffset = Encoder.getInstance().putAddress(this.buffer, this.paramOffset, struct);
        ++this.paramIndex;
    }

    public final void putObject(Object o, ObjectParameterStrategy strategy, ObjectParameterInfo info) {
        if (strategy.isDirect()) {
            this.paramOffset = Encoder.getInstance().putAddress(this.buffer, this.paramOffset, strategy.address(o));
        } else {
            this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
            this.getObjectBuffer().putObject(strategy.object(o), strategy.offset(o), strategy.length(o), ObjectBuffer.makeObjectFlags(info.ioflags(), strategy.typeInfo, this.paramIndex));
        }
        ++this.paramIndex;
    }

    public final void putObject(Object o, ObjectParameterStrategy strategy, int flags) {
        if (strategy.isDirect()) {
            this.paramOffset = Encoder.getInstance().putAddress(this.buffer, this.paramOffset, strategy.address(o));
        } else {
            this.paramOffset = Encoder.getInstance().skipAddress(this.paramOffset);
            this.getObjectBuffer().putObject(strategy.object(o), strategy.offset(o), strategy.length(o), ObjectBuffer.makeObjectFlags(flags, strategy.typeInfo, this.paramIndex));
        }
        ++this.paramIndex;
    }

    public final void putJNIEnvironment() {
        this.paramOffset = Encoder.getInstance().putAddress(this.buffer, this.paramOffset, 0L);
        this.getObjectBuffer().putJNI(this.paramIndex++, null, 0x1000000);
    }

    public final void putJNIObject(Object obj) {
        this.paramOffset = Encoder.getInstance().putAddress(this.buffer, this.paramOffset, 0L);
        this.getObjectBuffer().putJNI(this.paramIndex++, obj, 0x2000000);
    }

    static abstract class Encoder {
        Encoder() {
        }

        static Encoder getInstance() {
            return SingletonHolder.INSTANCE;
        }

        public abstract int getBufferSize(CallContext var1);

        public abstract int putByte(byte[] var1, int var2, int var3);

        public abstract int putShort(byte[] var1, int var2, int var3);

        public abstract int putInt(byte[] var1, int var2, int var3);

        public abstract int putLong(byte[] var1, int var2, long var3);

        public abstract int putFloat(byte[] var1, int var2, float var3);

        public abstract int putDouble(byte[] var1, int var2, double var3);

        public abstract int putAddress(byte[] var1, int var2, long var3);

        public abstract int skipAddress(int var1);

        private static class SingletonHolder {
            static final Encoder INSTANCE = new DefaultEncoder(ArrayIO.getInstance());

            private SingletonHolder() {
            }
        }
    }

    private static final class InvalidArrayIO
    extends ArrayIO {
        private final Throwable error;

        InvalidArrayIO(Throwable error) {
            this.error = error;
        }

        private RuntimeException ex() {
            RuntimeException ule = new RuntimeException("could not determine native data encoding");
            ule.initCause(this.error);
            return ule;
        }

        @Override
        public void putByte(byte[] buffer, int offset, int value) {
            throw this.ex();
        }

        @Override
        public void putShort(byte[] buffer, int offset, int value) {
            throw this.ex();
        }

        @Override
        public void putInt(byte[] buffer, int offset, int value) {
            throw this.ex();
        }

        @Override
        public void putLong(byte[] buffer, int offset, long value) {
            throw this.ex();
        }

        @Override
        public void putAddress(byte[] buffer, int offset, long value) {
            throw this.ex();
        }
    }

    private static final class BE64ArrayIO
    extends BigEndianArrayIO {
        static final ArrayIO INSTANCE = new BE64ArrayIO();

        private BE64ArrayIO() {
        }

        @Override
        public void putAddress(byte[] buffer, int offset, long value) {
            this.putLong(buffer, offset, value);
        }
    }

    private static final class BE32ArrayIO
    extends BigEndianArrayIO {
        static final ArrayIO INSTANCE = new BE32ArrayIO();

        private BE32ArrayIO() {
        }

        @Override
        public void putAddress(byte[] buffer, int offset, long value) {
            buffer[offset + 0] = (byte)(value >> 24);
            buffer[offset + 1] = (byte)(value >> 16);
            buffer[offset + 2] = (byte)(value >> 8);
            buffer[offset + 3] = (byte)value;
        }
    }

    private static abstract class BigEndianArrayIO
    extends ArrayIO {
        private BigEndianArrayIO() {
        }

        @Override
        public final void putByte(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte)value;
        }

        @Override
        public final void putShort(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte)(value >> 8);
            buffer[offset + 1] = (byte)value;
        }

        @Override
        public final void putInt(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte)(value >> 24);
            buffer[offset + 1] = (byte)(value >> 16);
            buffer[offset + 2] = (byte)(value >> 8);
            buffer[offset + 3] = (byte)value;
        }

        @Override
        public final void putLong(byte[] buffer, int offset, long value) {
            buffer[offset + 0] = (byte)(value >> 56);
            buffer[offset + 1] = (byte)(value >> 48);
            buffer[offset + 2] = (byte)(value >> 40);
            buffer[offset + 3] = (byte)(value >> 32);
            buffer[offset + 4] = (byte)(value >> 24);
            buffer[offset + 5] = (byte)(value >> 16);
            buffer[offset + 6] = (byte)(value >> 8);
            buffer[offset + 7] = (byte)value;
        }
    }

    private static final class LE64ArrayIO
    extends LittleEndianArrayIO {
        static final ArrayIO INSTANCE = new LE64ArrayIO();

        private LE64ArrayIO() {
        }

        @Override
        public final void putAddress(byte[] buffer, int offset, long value) {
            this.putLong(buffer, offset, value);
        }
    }

    private static final class LE32ArrayIO
    extends LittleEndianArrayIO {
        static final ArrayIO INSTANCE = new LE32ArrayIO();

        private LE32ArrayIO() {
        }

        @Override
        public final void putAddress(byte[] buffer, int offset, long value) {
            buffer[offset] = (byte)value;
            buffer[offset + 1] = (byte)(value >> 8);
            buffer[offset + 2] = (byte)(value >> 16);
            buffer[offset + 3] = (byte)(value >> 24);
        }
    }

    private static abstract class LittleEndianArrayIO
    extends ArrayIO {
        private LittleEndianArrayIO() {
        }

        @Override
        public final void putByte(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte)value;
        }

        @Override
        public final void putShort(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte)value;
            buffer[offset + 1] = (byte)(value >> 8);
        }

        @Override
        public final void putInt(byte[] buffer, int offset, int value) {
            buffer[offset] = (byte)value;
            buffer[offset + 1] = (byte)(value >> 8);
            buffer[offset + 2] = (byte)(value >> 16);
            buffer[offset + 3] = (byte)(value >> 24);
        }

        @Override
        public final void putLong(byte[] buffer, int offset, long value) {
            buffer[offset] = (byte)value;
            buffer[offset + 1] = (byte)(value >> 8);
            buffer[offset + 2] = (byte)(value >> 16);
            buffer[offset + 3] = (byte)(value >> 24);
            buffer[offset + 4] = (byte)(value >> 32);
            buffer[offset + 5] = (byte)(value >> 40);
            buffer[offset + 6] = (byte)(value >> 48);
            buffer[offset + 7] = (byte)(value >> 56);
        }
    }

    private static abstract class ArrayIO {
        private ArrayIO() {
        }

        static ArrayIO getInstance() {
            return SingletonHolder.DEFAULT;
        }

        static ArrayIO getBE32IO() {
            return BE32ArrayIO.INSTANCE;
        }

        static ArrayIO getLE32IO() {
            return LE32ArrayIO.INSTANCE;
        }

        static ArrayIO getLE64IO() {
            return LE64ArrayIO.INSTANCE;
        }

        static ArrayIO getBE64IO() {
            return BE64ArrayIO.INSTANCE;
        }

        static ArrayIO newInvalidArrayIO(Throwable error) {
            return new InvalidArrayIO(error);
        }

        public abstract void putByte(byte[] var1, int var2, int var3);

        public abstract void putShort(byte[] var1, int var2, int var3);

        public abstract void putInt(byte[] var1, int var2, int var3);

        public abstract void putLong(byte[] var1, int var2, long var3);

        public final void putFloat(byte[] buffer, int offset, float value) {
            this.putInt(buffer, offset, Float.floatToRawIntBits(value));
        }

        public final void putDouble(byte[] buffer, int offset, double value) {
            this.putLong(buffer, offset, Double.doubleToRawLongBits(value));
        }

        public abstract void putAddress(byte[] var1, int var2, long var3);

        private static final class SingletonHolder {
            private static final ArrayIO DEFAULT;

            private SingletonHolder() {
            }

            static {
                ArrayIO io;
                try {
                    switch (Platform.getPlatform().addressSize()) {
                        case 32: {
                            io = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN) ? ArrayIO.getBE32IO() : ArrayIO.getLE32IO();
                            break;
                        }
                        case 64: {
                            io = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN) ? ArrayIO.getBE64IO() : ArrayIO.getLE64IO();
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("unsupported address size: " + Platform.getPlatform().addressSize());
                        }
                    }
                }
                catch (Throwable error) {
                    io = ArrayIO.newInvalidArrayIO(error);
                }
                DEFAULT = io;
            }
        }
    }

    private static final class DefaultEncoder
    extends Encoder {
        private final ArrayIO io;

        public DefaultEncoder(ArrayIO io) {
            this.io = io;
        }

        @Override
        public final int getBufferSize(CallContext callContext) {
            return callContext.getParameterCount() * 8;
        }

        @Override
        public final int putByte(byte[] buffer, int offset, int value) {
            this.io.putByte(buffer, offset, value);
            return offset + 8;
        }

        @Override
        public final int putShort(byte[] buffer, int offset, int value) {
            this.io.putShort(buffer, offset, value);
            return offset + 8;
        }

        @Override
        public final int putInt(byte[] buffer, int offset, int value) {
            this.io.putInt(buffer, offset, value);
            return offset + 8;
        }

        @Override
        public final int putLong(byte[] buffer, int offset, long value) {
            this.io.putLong(buffer, offset, value);
            return offset + 8;
        }

        @Override
        public final int putFloat(byte[] buffer, int offset, float value) {
            this.io.putFloat(buffer, offset, value);
            return offset + 8;
        }

        @Override
        public final int putDouble(byte[] buffer, int offset, double value) {
            this.io.putDouble(buffer, offset, value);
            return offset + 8;
        }

        @Override
        public final int putAddress(byte[] buffer, int offset, long value) {
            this.io.putAddress(buffer, offset, value);
            return offset + 8;
        }

        @Override
        public int skipAddress(int offset) {
            return offset + 8;
        }
    }
}

