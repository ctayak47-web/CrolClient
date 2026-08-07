
package jnr.ffi.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeType;

public final class CompositeTypeMapper
implements SignatureTypeMapper {
    private final Collection<SignatureTypeMapper> signatureTypeMappers;

    public CompositeTypeMapper(SignatureTypeMapper ... signatureTypeMappers) {
        this.signatureTypeMappers = Collections.unmodifiableList(Arrays.asList((SignatureTypeMapper[])signatureTypeMappers.clone()));
    }

    public CompositeTypeMapper(Collection<SignatureTypeMapper> signatureTypeMappers) {
        this.signatureTypeMappers = Collections.unmodifiableList(new ArrayList<SignatureTypeMapper>(signatureTypeMappers));
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        for (SignatureTypeMapper m : this.signatureTypeMappers) {
            FromNativeType fromNativeType = m.getFromNativeType(type, context);
            if (fromNativeType == null) continue;
            return fromNativeType;
        }
        return null;
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        for (SignatureTypeMapper m : this.signatureTypeMappers) {
            ToNativeType toNativeType = m.getToNativeType(type, context);
            if (toNativeType == null) continue;
            return toNativeType;
        }
        return null;
    }
}

