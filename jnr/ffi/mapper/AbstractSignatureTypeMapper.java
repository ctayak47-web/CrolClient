
package jnr.ffi.mapper;

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeType;

public abstract class AbstractSignatureTypeMapper
implements SignatureTypeMapper {
    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        return null;
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        return null;
    }
}

