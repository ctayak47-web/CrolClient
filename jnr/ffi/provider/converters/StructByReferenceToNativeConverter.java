
package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public final class StructByReferenceToNativeConverter
implements ToNativeConverter<Struct, Pointer> {
    private final int flags;

    public static ToNativeConverter<Struct, Pointer> getInstance(ToNativeContext toNativeContext) {
        return new StructByReferenceToNativeConverter(ParameterFlags.parse(toNativeContext.getAnnotations()));
    }

    StructByReferenceToNativeConverter(int flags) {
        this.flags = flags;
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    @Override
    public Pointer toNative(Struct value, ToNativeContext ctx) {
        return value != null ? Struct.getMemory(value, this.flags) : null;
    }
}

