
package com.kenai.jffi;

import com.kenai.jffi.Foreign;
import com.kenai.jffi.NativeType;
import java.util.List;

public abstract class Type {
    public static final Type VOID = Type.builtin(NativeType.VOID);
    public static final Type FLOAT = Type.builtin(NativeType.FLOAT);
    public static final Type DOUBLE = Type.builtin(NativeType.DOUBLE);
    public static final Type LONGDOUBLE = Type.builtin(NativeType.LONGDOUBLE);
    public static final Type UINT8 = Type.builtin(NativeType.UINT8);
    public static final Type SINT8 = Type.builtin(NativeType.SINT8);
    public static final Type UINT16 = Type.builtin(NativeType.UINT16);
    public static final Type SINT16 = Type.builtin(NativeType.SINT16);
    public static final Type UINT32 = Type.builtin(NativeType.UINT32);
    public static final Type SINT32 = Type.builtin(NativeType.SINT32);
    public static final Type UINT64 = Type.builtin(NativeType.UINT64);
    public static final Type SINT64 = Type.builtin(NativeType.SINT64);
    public static final Type POINTER = Type.builtin(NativeType.POINTER);
    public static final Type UCHAR = UINT8;
    public static final Type SCHAR = SINT8;
    public static final Type USHORT = UINT16;
    public static final Type SSHORT = SINT16;
    public static final Type UINT = UINT32;
    public static final Type SINT = SINT32;
    public static final Type ULONG = Type.builtin(NativeType.ULONG);
    public static final Type SLONG = Type.builtin(NativeType.SLONG);
    public static final Type ULONG_LONG = UINT64;
    public static final Type SLONG_LONG = SINT64;
    private int type = 0;
    private int size = 0;
    private int alignment = 0;
    private volatile long handle = 0L;

    public final int type() {
        return this.type != 0 ? this.type : this.resolveType();
    }

    final long handle() {
        return this.handle != 0L ? this.handle : this.resolveHandle();
    }

    public final int size() {
        return this.size != 0 ? this.size : this.resolveSize();
    }

    public final int alignment() {
        return this.alignment != 0 ? this.alignment : this.resolveAlignment();
    }

    private int resolveType() {
        this.type = this.getTypeInfo().type;
        return this.type;
    }

    private int resolveSize() {
        this.size = this.getTypeInfo().size;
        return this.size;
    }

    private int resolveAlignment() {
        this.alignment = this.getTypeInfo().alignment;
        return this.alignment;
    }

    private long resolveHandle() {
        this.handle = this.getTypeInfo().handle;
        return this.handle;
    }

    abstract TypeInfo getTypeInfo();

    public boolean equals(Object obj) {
        return obj instanceof Type && ((Type)obj).handle() == this.handle();
    }

    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (int)(this.handle() ^ this.handle() >>> 32);
        return hash;
    }

    static long[] nativeHandles(Type[] types) {
        long[] nativeTypes = new long[types.length];
        for (int i = 0; i < types.length; ++i) {
            nativeTypes[i] = types[i].handle();
        }
        return nativeTypes;
    }

    static long[] nativeHandles(List<Type> types) {
        long[] nativeTypes = new long[types.size()];
        for (int i = 0; i < nativeTypes.length; ++i) {
            nativeTypes[i] = types.get(i).handle();
        }
        return nativeTypes;
    }

    private static Type builtin(NativeType nativeType) {
        return new Builtin(nativeType);
    }

    static final class TypeInfo {
        final int type;
        final int size;
        final int alignment;
        final long handle;

        TypeInfo(long handle, int type, int size, int alignment) {
            this.handle = handle;
            this.type = type;
            this.size = size;
            this.alignment = alignment;
        }
    }

    static final class Builtin
    extends Type {
        private final NativeType nativeType;
        private TypeInfo typeInfo;

        private Builtin(NativeType nativeType) {
            this.nativeType = nativeType;
        }

        @Override
        TypeInfo getTypeInfo() {
            return this.typeInfo != null ? this.typeInfo : this.lookupTypeInfo();
        }

        private TypeInfo lookupTypeInfo() {
            try {
                Foreign foreign = Foreign.getInstance();
                long handle = foreign.lookupBuiltinType(this.nativeType.ffiType);
                if (handle == 0L) {
                    throw new NullPointerException("invalid handle for native type " + (Object)((Object)this.nativeType));
                }
                this.typeInfo = new TypeInfo(handle, foreign.getTypeType(handle), foreign.getTypeSize(handle), foreign.getTypeAlign(handle));
                return this.typeInfo;
            }
            catch (Throwable error) {
                throw new UnsatisfiedLinkError("could not get native definition for type `" + (Object)((Object)this.nativeType) + "`, original error message follows: " + error.getLocalizedMessage());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Builtin builtin = (Builtin)o;
            return this.nativeType == builtin.nativeType;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + this.nativeType.hashCode();
            return result;
        }
    }
}

