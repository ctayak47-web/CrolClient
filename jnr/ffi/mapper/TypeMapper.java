
package jnr.ffi.mapper;

import java.util.HashMap;
import java.util.Map;
import jnr.ffi.mapper.DataConverter;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.SimpleTypeMapper;
import jnr.ffi.mapper.ToNativeConverter;

public interface TypeMapper {
    public FromNativeConverter getFromNativeConverter(Class var1);

    public ToNativeConverter getToNativeConverter(Class var1);

    public static final class Builder {
        private final Map<Class, ToNativeConverter<?, ?>> toNativeConverterMap = new HashMap();
        private final Map<Class, FromNativeConverter<?, ?>> fromNativeConverterMap = new HashMap();

        public <T> Builder map(Class<? extends T> javaType, ToNativeConverter<? extends T, ?> toNativeConverter) {
            this.toNativeConverterMap.put(javaType, toNativeConverter);
            return this;
        }

        public <T> Builder map(Class<? extends T> javaType, FromNativeConverter<? extends T, ?> fromNativeConverter) {
            this.fromNativeConverterMap.put(javaType, fromNativeConverter);
            return this;
        }

        public <T> Builder map(Class<? extends T> javaType, DataConverter<? extends T, ?> dataConverter) {
            this.toNativeConverterMap.put(javaType, dataConverter);
            this.fromNativeConverterMap.put(javaType, dataConverter);
            return this;
        }

        public TypeMapper build() {
            return new SimpleTypeMapper(this.toNativeConverterMap, this.fromNativeConverterMap);
        }
    }
}

