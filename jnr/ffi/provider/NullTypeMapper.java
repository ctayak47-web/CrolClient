
package jnr.ffi.provider;

import jnr.ffi.mapper.AbstractSignatureTypeMapper;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;
import jnr.ffi.provider.FromNativeType;
import jnr.ffi.provider.ToNativeType;

public class NullTypeMapper
extends AbstractSignatureTypeMapper
implements TypeMapper,
SignatureTypeMapper {
    @Override
    public FromNativeConverter getFromNativeConverter(Class type) {
        return null;
    }

    @Override
    public ToNativeConverter getToNativeConverter(Class type) {
        return null;
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        return null;
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        return null;
    }
}

