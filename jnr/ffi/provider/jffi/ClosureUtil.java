
package jnr.ffi.provider.jffi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import jnr.ffi.NativeType;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.mapper.DefaultSignatureType;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.FromNativeType;
import jnr.ffi.provider.ToNativeType;
import jnr.ffi.provider.jffi.SimpleNativeContext;
import jnr.ffi.provider.jffi.Types;
import jnr.ffi.util.Annotations;

final class ClosureUtil {
    private ClosureUtil() {
    }

    static ToNativeType getResultType(Runtime runtime, Method m, SignatureTypeMapper typeMapper) {
        Collection<Annotation> annotations = Annotations.sortedAnnotationCollection(m.getAnnotations());
        SimpleNativeContext context = new SimpleNativeContext(runtime, annotations);
        DefaultSignatureType signatureType = DefaultSignatureType.create(m.getReturnType(), context);
        jnr.ffi.mapper.ToNativeType toNativeType = typeMapper.getToNativeType(signatureType, context);
        ToNativeConverter converter = toNativeType != null ? toNativeType.getToNativeConverter() : null;
        Class<Object> javaClass = converter != null ? converter.nativeType() : m.getReturnType();
        NativeType nativeType = Types.getType(runtime, javaClass, annotations).getNativeType();
        return new ToNativeType(m.getReturnType(), nativeType, annotations, converter, context);
    }

    static FromNativeType getParameterType(Runtime runtime, Method m, int idx, SignatureTypeMapper typeMapper) {
        SimpleNativeContext context;
        Collection<Annotation> annotations = Annotations.sortedAnnotationCollection(m.getParameterAnnotations()[idx]);
        Class<?> declaredJavaClass = m.getParameterTypes()[idx];
        DefaultSignatureType signatureType = new DefaultSignatureType(declaredJavaClass, (context = new SimpleNativeContext(runtime, annotations)).getAnnotations(), m.getGenericParameterTypes()[idx]);
        jnr.ffi.mapper.FromNativeType fromNativeType = typeMapper.getFromNativeType(signatureType, context);
        FromNativeConverter converter = fromNativeType != null ? fromNativeType.getFromNativeConverter() : null;
        Class<Object> javaClass = converter != null ? converter.nativeType() : declaredJavaClass;
        NativeType nativeType = Types.getType(runtime, javaClass, annotations).getNativeType();
        return new FromNativeType(declaredJavaClass, nativeType, annotations, converter, context);
    }

    static Method getDelegateMethod(Class closureClass) {
        Method callMethod = null;
        for (Method m : closureClass.getMethods()) {
            if (!m.isAnnotationPresent(Delegate.class) || !Modifier.isPublic(m.getModifiers()) || Modifier.isStatic(m.getModifiers())) continue;
            callMethod = m;
            break;
        }
        if (callMethod == null) {
            throw new NoSuchMethodError("no public non-static delegate method defined in " + closureClass.getName());
        }
        return callMethod;
    }
}

