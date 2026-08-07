
package jnr.ffi.mapper;

import java.util.LinkedHashMap;
import java.util.Map;
import jnr.ffi.mapper.DataConverter;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;

public final class DefaultTypeMapper
implements TypeMapper {
    private final Map<Class, ToNativeConverter> toNativeConverters = new LinkedHashMap<Class, ToNativeConverter>();
    private final Map<Class, FromNativeConverter> fromNativeConverters = new LinkedHashMap<Class, FromNativeConverter>();

    public final void put(Class javaClass, DataConverter converter) {
        this.toNativeConverters.put(javaClass, converter);
        this.fromNativeConverters.put(javaClass, converter);
    }

    public final void put(Class javaClass, ToNativeConverter converter) {
        this.toNativeConverters.put(javaClass, converter);
    }

    public final void put(Class javaClass, FromNativeConverter converter) {
        this.fromNativeConverters.put(javaClass, converter);
    }

    @Override
    public FromNativeConverter getFromNativeConverter(Class type) {
        return this.fromNativeConverters.get(type);
    }

    @Override
    public ToNativeConverter getToNativeConverter(Class type) {
        return this.toNativeConverters.get(type);
    }
}

