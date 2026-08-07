
package jnr.ffi.mapper;

import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.FromNativeType;

public abstract class AbstractFromNativeType
implements FromNativeType {
    private final FromNativeConverter converter;

    AbstractFromNativeType(FromNativeConverter converter) {
        this.converter = converter;
    }

    @Override
    public FromNativeConverter getFromNativeConverter() {
        return this.converter;
    }
}

