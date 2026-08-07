
package jnr.ffi.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jnr.ffi.mapper.FromNativeContext;

public interface FromNativeConverter<J, N> {
    public J fromNative(N var1, FromNativeContext var2);

    public Class<N> nativeType();

    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.METHOD})
    public static @interface FromNative {
        public Class nativeType();
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.TYPE})
    public static @interface Cacheable {
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.TYPE, ElementType.METHOD})
    public static @interface NoContext {
    }
}

