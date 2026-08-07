
package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

public interface SignatureType {
    public Class getDeclaredType();

    public Collection<Annotation> getAnnotations();

    public Type getGenericType();
}

