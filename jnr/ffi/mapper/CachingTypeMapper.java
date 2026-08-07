
package jnr.ffi.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jnr.ffi.mapper.AbstractSignatureTypeMapper;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.ToNativeType;

public final class CachingTypeMapper
extends AbstractSignatureTypeMapper
implements SignatureTypeMapper {
    private final SignatureTypeMapper mapper;
    private volatile Map<SignatureType, ToNativeType> toNativeTypeMap = Collections.emptyMap();
    private volatile Map<SignatureType, FromNativeType> fromNativeTypeMap = Collections.emptyMap();
    private static final InvalidType UNCACHEABLE_TYPE = new InvalidType();
    private static final InvalidType NO_TYPE = new InvalidType();

    public CachingTypeMapper(SignatureTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        FromNativeType fromNativeType = this.fromNativeTypeMap.get(type);
        if (fromNativeType == UNCACHEABLE_TYPE) {
            return this.mapper.getFromNativeType(type, context);
        }
        if (fromNativeType == NO_TYPE) {
            return null;
        }
        return fromNativeType != null ? fromNativeType : this.lookupAndCacheFromNativeType(type, context);
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        ToNativeType toNativeType = this.toNativeTypeMap.get(type);
        if (toNativeType == UNCACHEABLE_TYPE) {
            return this.mapper.getToNativeType(type, context);
        }
        if (toNativeType == NO_TYPE) {
            return null;
        }
        return toNativeType != null ? toNativeType : this.lookupAndCacheToNativeType(type, context);
    }

    private synchronized FromNativeType lookupAndCacheFromNativeType(SignatureType signature, FromNativeContext context) {
        FromNativeType fromNativeType = this.fromNativeTypeMap.get(signature);
        if (fromNativeType == null) {
            FromNativeType typeForCaching = fromNativeType = this.mapper.getFromNativeType(signature, context);
            if (fromNativeType == null) {
                typeForCaching = NO_TYPE;
            } else if (!fromNativeType.getClass().isAnnotationPresent(FromNativeType.Cacheable.class)) {
                typeForCaching = UNCACHEABLE_TYPE;
            }
            HashMap<SignatureType, FromNativeType> m = new HashMap<SignatureType, FromNativeType>(this.fromNativeTypeMap.size() + 1);
            m.putAll(this.fromNativeTypeMap);
            m.put(signature, typeForCaching);
            this.fromNativeTypeMap = Collections.unmodifiableMap(m);
        }
        return fromNativeType != NO_TYPE ? fromNativeType : null;
    }

    private synchronized ToNativeType lookupAndCacheToNativeType(SignatureType signature, ToNativeContext context) {
        ToNativeType toNativeType = this.toNativeTypeMap.get(signature);
        if (toNativeType == null) {
            ToNativeType typeForCaching = toNativeType = this.mapper.getToNativeType(signature, context);
            if (toNativeType == null) {
                typeForCaching = NO_TYPE;
            } else if (!toNativeType.getClass().isAnnotationPresent(ToNativeType.Cacheable.class)) {
                typeForCaching = UNCACHEABLE_TYPE;
            }
            HashMap<SignatureType, ToNativeType> m = new HashMap<SignatureType, ToNativeType>(this.toNativeTypeMap.size() + 1);
            m.putAll(this.toNativeTypeMap);
            m.put(signature, typeForCaching);
            this.toNativeTypeMap = Collections.unmodifiableMap(m);
        }
        return toNativeType != NO_TYPE ? toNativeType : null;
    }

    private static final class InvalidType
    implements ToNativeType,
    FromNativeType {
        private InvalidType() {
        }

        @Override
        public FromNativeConverter getFromNativeConverter() {
            return null;
        }

        @Override
        public ToNativeConverter getToNativeConverter() {
            return null;
        }
    }
}

