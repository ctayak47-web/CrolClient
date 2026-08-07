
package jnr.ffi.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jnr.ffi.mapper.ToNativeContext;

public interface ToNativeConverter<J, N> {
    public N toNative(J var1, ToNativeContext var2);

    public Class<N> nativeType();

    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.METHOD})
    public static @interface ToNative {
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

    public static interface PostInvocation<J, N>
    extends ToNativeConverter<J, N> {
        public void postInvoke(J var1, N var2, ToNativeContext var3);
    }
}

