
package jnr.ffi.provider;

import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.EnumSet;
import jnr.ffi.NativeType;
import jnr.ffi.Runtime;
import jnr.ffi.Type;
import jnr.ffi.provider.BadType;
import jnr.ffi.provider.MemoryManager;

public abstract class AbstractRuntime
extends Runtime {
    private final Type[] types;
    private final long addressMask;
    private final int addressSize;
    private final int longSize;
    private final ByteOrder byteOrder;

    public AbstractRuntime(ByteOrder byteOrder, EnumMap<NativeType, Type> typeMap) {
        this.byteOrder = byteOrder;
        EnumSet<NativeType> nativeTypes = EnumSet.allOf(NativeType.class);
        this.types = new Type[nativeTypes.size()];
        for (NativeType t : nativeTypes) {
            this.types[t.ordinal()] = typeMap.containsKey((Object)t) ? typeMap.get((Object)t) : new BadType(t.toString());
        }
        this.addressSize = this.types[NativeType.ADDRESS.ordinal()].size();
        this.longSize = this.types[NativeType.SLONG.ordinal()].size();
        this.addressMask = this.addressSize == 4 ? 0xFFFFFFFFL : -1L;
    }

    @Override
    public final Type findType(NativeType type) {
        return this.types[type.ordinal()];
    }

    @Override
    public abstract MemoryManager getMemoryManager();

    @Override
    public abstract int getLastError();

    @Override
    public abstract void setLastError(int var1);

    @Override
    public final long addressMask() {
        return this.addressMask;
    }

    @Override
    public final int addressSize() {
        return this.addressSize;
    }

    @Override
    public final int longSize() {
        return this.longSize;
    }

    @Override
    public final ByteOrder byteOrder() {
        return this.byteOrder;
    }
}

