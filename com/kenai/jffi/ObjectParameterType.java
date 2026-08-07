
package com.kenai.jffi;

import java.util.EnumSet;

public final class ObjectParameterType {
    final int typeInfo;
    static final ObjectParameterType INVALID = new ObjectParameterType(0);
    static final ObjectParameterType NONE = new ObjectParameterType(0);
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

    public static ObjectParameterType create(ObjectType objectType, ComponentType componentType) {
        if (objectType == ObjectType.ARRAY) {
            return TypeCache.arrayTypeCache[componentType.ordinal()];
        }
        if (objectType == ObjectType.BUFFER) {
            return TypeCache.bufferTypeCache[componentType.ordinal()];
        }
        return new ObjectParameterType(objectType.value | componentType.value);
    }

    ObjectParameterType(int typeInfo) {
        this.typeInfo = typeInfo;
    }

    ObjectParameterType(ObjectType objectType, ComponentType componentType) {
        this.typeInfo = objectType.value | componentType.value;
    }

    public boolean equals(Object o) {
        return this == o || o instanceof ObjectParameterType && this.typeInfo == ((ObjectParameterType)o).typeInfo;
    }

    public int hashCode() {
        return this.typeInfo;
    }

    public static enum ObjectType {
        ARRAY(0x10000000),
        BUFFER(0x20000000);

        final int value;

        private ObjectType(int type) {
            this.value = type;
        }
    }

    private static final class TypeCache {
        static final ObjectParameterType[] arrayTypeCache;
        static final ObjectParameterType[] bufferTypeCache;

        private TypeCache() {
        }

        static {
            EnumSet<ComponentType> componentTypes = EnumSet.allOf(ComponentType.class);
            arrayTypeCache = new ObjectParameterType[componentTypes.size()];
            bufferTypeCache = new ObjectParameterType[componentTypes.size()];
            for (ComponentType componentType : componentTypes) {
                TypeCache.arrayTypeCache[componentType.ordinal()] = new ObjectParameterType(ARRAY, componentType);
                TypeCache.bufferTypeCache[componentType.ordinal()] = new ObjectParameterType(BUFFER, componentType);
            }
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

