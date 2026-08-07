
package jnr.ffi.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import jnr.ffi.NativeType;
import jnr.ffi.mapper.SignatureType;

public abstract class SigType
implements SignatureType {
    private final Class javaType;
    private final Class convertedType;
    private final Collection<Annotation> annotations;
    private final NativeType nativeType;

    public SigType(Class javaType, NativeType nativeType, Collection<Annotation> annotations, Class convertedType) {
        this.javaType = javaType;
        this.annotations = annotations;
        this.convertedType = convertedType;
        this.nativeType = nativeType;
    }

    @Override
    public final Class getDeclaredType() {
        return this.javaType;
    }

    public final Class effectiveJavaType() {
        return this.convertedType;
    }

    public final Collection<Annotation> annotations() {
        return this.annotations;
    }

    @Override
    public final Collection<Annotation> getAnnotations() {
        return this.annotations;
    }

    @Override
    public Type getGenericType() {
        return this.getDeclaredType();
    }

    public final String toString() {
        return String.format("declared: %s, effective: %s, native: %s", new Object[]{this.getDeclaredType(), this.effectiveJavaType(), this.getNativeType()});
    }

    public NativeType getNativeType() {
        return this.nativeType;
    }
}

