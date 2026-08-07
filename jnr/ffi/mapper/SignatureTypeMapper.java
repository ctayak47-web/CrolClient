
package jnr.ffi.mapper;

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeType;

public interface SignatureTypeMapper {
    public FromNativeType getFromNativeType(SignatureType var1, FromNativeContext var2);

    public ToNativeType getToNativeType(SignatureType var1, ToNativeContext var2);
}

