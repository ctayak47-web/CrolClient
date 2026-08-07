
package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.MethodParameterContext;
import jnr.ffi.mapper.MethodResultContext;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.util.Annotations;

public final class DefaultSignatureType
implements SignatureType {
    private final Class declaredClass;
    private final Collection<Annotation> annotations;
    private final Type genericType;

    public DefaultSignatureType(Class declaredClass, Collection<Annotation> annotations, Type genericType) {
        this.declaredClass = declaredClass;
        this.annotations = Annotations.sortedAnnotationCollection(annotations);
        this.genericType = genericType;
    }

    @Override
    public Class getDeclaredType() {
        return this.declaredClass;
    }

    @Override
    public Collection<Annotation> getAnnotations() {
        return this.annotations;
    }

    @Override
    public Type getGenericType() {
        return this.genericType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DefaultSignatureType signature = (DefaultSignatureType)o;
        return this.declaredClass == signature.declaredClass && this.genericType.equals(signature.genericType) && this.annotations.equals(signature.annotations);
    }

    public int hashCode() {
        int result = this.declaredClass.hashCode();
        result = 31 * result + this.annotations.hashCode();
        if (this.genericType != null) {
            result = 31 * result + this.genericType.hashCode();
        }
        return result;
    }

    public static DefaultSignatureType create(Class type, FromNativeContext context) {
        Class genericType = !type.isPrimitive() && context instanceof MethodResultContext ? ((MethodResultContext)context).getMethod().getGenericReturnType() : type;
        return new DefaultSignatureType(type, context.getAnnotations(), genericType);
    }

    public static DefaultSignatureType create(Class type, ToNativeContext context) {
        Type genericType = type;
        if (!type.isPrimitive() && context instanceof MethodParameterContext) {
            MethodParameterContext methodParameterContext = (MethodParameterContext)context;
            genericType = methodParameterContext.getMethod().getGenericParameterTypes()[methodParameterContext.getParameterIndex()];
        }
        return new DefaultSignatureType(type, context.getAnnotations(), genericType);
    }
}

