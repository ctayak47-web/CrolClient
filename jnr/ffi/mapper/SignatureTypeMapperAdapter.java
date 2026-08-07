
package jnr.ffi.mapper;

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.FromNativeTypes;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeType;
import jnr.ffi.mapper.ToNativeTypes;
import jnr.ffi.mapper.TypeMapper;

public class SignatureTypeMapperAdapter
implements SignatureTypeMapper {
    private final TypeMapper typeMapper;

    public SignatureTypeMapperAdapter(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        return FromNativeTypes.create(this.typeMapper.getFromNativeConverter(type.getDeclaredType()));
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        return ToNativeTypes.create(this.typeMapper.getToNativeConverter(type.getDeclaredType()));
    }
}

