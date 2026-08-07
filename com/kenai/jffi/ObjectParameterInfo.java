
package com.kenai.jffi;

import com.kenai.jffi.ObjectBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ObjectParameterInfo {
    private static final ConcurrentMap<Integer, ObjectParameterInfo> CACHE = new ConcurrentHashMap<Integer, ObjectParameterInfo>();
    private final int parameterIndex;
    private final int ioflags;
    private final int objectInfo;
    public static final int IN = 1;
    public static final int OUT = 2;
    public static final int PINNED = 8;
    public static final int NULTERMINATE = 4;
    public static final int CLEAR = 16;
    public static final ObjectType ARRAY = ObjectType.ARRAY;
    public static final ObjectType BUFFER = ObjectType.BUFFER;
    public static final ComponentType BYTE = ComponentType.BYTE;
    public static final ComponentType SHORT = ComponentType.SHORT;
    public static final ComponentType INT = ComponentType.INT;
    public static final ComponentType LONG = ComponentType.LONG;
    public static final ComponentType FLOAT = ComponentType.FLOAT;
    public static final ComponentType DOUBLE = ComponentType.DOUBLE;
    public static final ComponentType BOOLEAN = ComponentType.BOOLEAN;
    public static final ComponentType CHAR = ComponentType.CHAR;

    public static ObjectParameterInfo create(int parameterIndex, ObjectType objectType, ComponentType componentType, int ioflags) {
        return ObjectParameterInfo.getCachedInfo(ObjectBuffer.makeObjectFlags(ioflags, objectType.value | componentType.value, parameterIndex));
    }

    public static ObjectParameterInfo create(int parameterIndex, int ioflags) {
        return ObjectParameterInfo.getCachedInfo(ObjectBuffer.makeObjectFlags(ioflags, 0, parameterIndex));
    }

    private static ObjectParameterInfo getCachedInfo(int objectInfo) {
        ObjectParameterInfo info = (ObjectParameterInfo)CACHE.get(objectInfo);
        if (info != null) {
            return info;
        }
        info = new ObjectParameterInfo(objectInfo);
        ObjectParameterInfo cachedInfo = CACHE.putIfAbsent(objectInfo, info);
        return cachedInfo != null ? cachedInfo : info;
    }

    private ObjectParameterInfo(int objectInfo) {
        this.objectInfo = objectInfo;
        this.ioflags = objectInfo & 0xFF;
        this.parameterIndex = (objectInfo & 0xFF0000) >> 16;
    }

    final int asObjectInfo() {
        return this.objectInfo;
    }

    final int ioflags() {
        return this.ioflags;
    }

    public final int getParameterIndex() {
        return this.parameterIndex;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ObjectParameterInfo info = (ObjectParameterInfo)o;
        return this.objectInfo == info.objectInfo;
    }

    public int hashCode() {
        return 31 * this.objectInfo;
    }

    public static enum ObjectType {
        ARRAY(0x10000000),
        BUFFER(0x20000000);

        final int value;

        private ObjectType(int type) {
            this.value = type;
        }
    }

    public static enum ComponentType {
        BYTE(0x1000000),
        SHORT(0x2000000),
        INT(0x3000000),
        LONG(0x4000000),
        FLOAT(0x5000000),
        DOUBLE(0x6000000),
        BOOLEAN(0x7000000),
        CHAR(0x8000000);

        final int value;

        private ComponentType(int type) {
            this.value = type;
        }
    }
}

