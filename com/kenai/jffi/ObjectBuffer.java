
package com.kenai.jffi;

import java.nio.Buffer;

final class ObjectBuffer {
    public static final int IN = 1;
    public static final int OUT = 2;
    public static final int ZERO_TERMINATE = 4;
    public static final int PINNED = 8;
    public static final int CLEAR = 16;
    static final int INDEX_SHIFT = 16;
    static final int INDEX_MASK = 0xFF0000;
    static final int TYPE_SHIFT = 24;
    static final int TYPE_MASK = -16777216;
    static final int PRIM_MASK = 0xF000000;
    static final int FLAGS_SHIFT = 0;
    static final int FLAGS_MASK = 255;
    static final int ARRAY = 0x10000000;
    static final int BUFFER = 0x20000000;
    static final int JNI = 0x40000000;
    static final int BYTE = 0x1000000;
    static final int SHORT = 0x2000000;
    static final int INT = 0x3000000;
    static final int LONG = 0x4000000;
    static final int FLOAT = 0x5000000;
    static final int DOUBLE = 0x6000000;
    static final int BOOLEAN = 0x7000000;
    static final int CHAR = 0x8000000;
    public static final int JNIENV = 0x1000000;
    public static final int JNIOBJECT = 0x2000000;
    private Object[] objects;
    private int[] info;
    private int infoIndex = 0;
    private int objectIndex = 0;

    ObjectBuffer() {
        this.objects = new Object[1];
        this.info = new int[this.objects.length * 3];
    }

    ObjectBuffer(int objectCount) {
        this.objects = new Object[objectCount];
        this.info = new int[objectCount * 3];
    }

    final int objectCount() {
        return this.objectIndex;
    }

    final int[] info() {
        return this.info;
    }

    final Object[] objects() {
        return this.objects;
    }

    private final void ensureSpace() {
        if (this.objects.length <= this.objectIndex + 1) {
            Object[] newObjects = new Object[this.objects.length << 1];
            System.arraycopy(this.objects, 0, newObjects, 0, this.objectIndex);
            this.objects = newObjects;
            int[] newInfo = new int[this.objects.length * 3];
            System.arraycopy(this.info, 0, newInfo, 0, this.objectIndex * 3);
            this.info = newInfo;
        }
    }

    static final int makeObjectFlags(int ioflags, int type, int index) {
        return ioflags & 0xFF | index << 16 & 0xFF0000 | type;
    }

    static final int makeBufferFlags(int index) {
        return index << 16 & 0xFF0000 | 0x20000000;
    }

    private static final int makeJNIFlags(int index, int type) {
        return index << 16 & 0xFF0000 | 0x40000000 | type;
    }

    public void putArray(int index, byte[] array, int offset, int length, int flags) {
        this.putObject(array, offset, length, ObjectBuffer.makeObjectFlags(flags, 0x11000000, index));
    }

    public void putArray(int index, short[] array, int offset, int length, int flags) {
        this.putObject(array, offset, length, ObjectBuffer.makeObjectFlags(flags, 0x12000000, index));
    }

    public void putArray(int index, int[] array, int offset, int length, int flags) {
        this.putObject(array, offset, length, ObjectBuffer.makeObjectFlags(flags, 0x13000000, index));
    }

    public void putArray(int index, long[] array, int offset, int length, int flags) {
        this.putObject(array, offset, length, ObjectBuffer.makeObjectFlags(flags, 0x14000000, index));
    }

    public void putArray(int index, float[] array, int offset, int length, int flags) {
        this.putObject(array, offset, length, ObjectBuffer.makeObjectFlags(flags, 0x15000000, index));
    }

    public void putArray(int index, double[] array, int offset, int length, int flags) {
        this.putObject(array, offset, length, ObjectBuffer.makeObjectFlags(flags, 0x16000000, index));
    }

    public void putArray(int index, boolean[] array, int offset, int length, int flags) {
        this.putObject(array, offset, length, ObjectBuffer.makeObjectFlags(flags, 0x17000000, index));
    }

    public void putArray(int index, char[] array, int offset, int length, int flags) {
        this.putObject(array, offset, length, ObjectBuffer.makeObjectFlags(flags, 0x18000000, index));
    }

    public void putDirectBuffer(int index, Buffer obj, int offset, int length) {
        this.putObject(obj, offset, length, ObjectBuffer.makeBufferFlags(index));
    }

    public void putJNI(int index, Object obj, int type) {
        this.putObject(obj, 0, 0, ObjectBuffer.makeJNIFlags(index, type));
    }

    void putObject(Object array, int offset, int length, int flags) {
        this.ensureSpace();
        this.objects[this.objectIndex++] = array;
        this.info[this.infoIndex++] = flags;
        this.info[this.infoIndex++] = offset;
        this.info[this.infoIndex++] = length;
    }
}

