
package jnr.ffi.mapper;

import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.ToNativeType;

public abstract class AbstractToNativeType
implements ToNativeType {
    private final ToNativeConverter converter;

    AbstractToNativeType(ToNativeConverter converter) {
        this.converter = converter;
    }

    @Override
    public ToNativeConverter getToNativeConverter() {
        return this.converter;
    }
}

