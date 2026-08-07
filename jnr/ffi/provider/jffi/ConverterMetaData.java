
package jnr.ffi.provider.jffi;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.util.Annotations;

class ConverterMetaData {
    private static volatile Reference<Map<Class, ConverterMetaData>> cacheReference;
    final Collection<Annotation> classAnnotations;
    final Collection<Annotation> toNativeMethodAnnotations;
    final Collection<Annotation> fromNativeMethodAnnotations;
    final Collection<Annotation> nativeTypeMethodAnnotations;
    final Collection<Annotation> toNativeAnnotations;
    final Collection<Annotation> fromNativeAnnotations;

    ConverterMetaData(Class converterClass, Class nativeType) {
        this.classAnnotations = Annotations.sortedAnnotationCollection(converterClass.getAnnotations());
        this.nativeTypeMethodAnnotations = ConverterMetaData.getConverterMethodAnnotations(converterClass, "nativeType", new Class[0]);
        this.fromNativeMethodAnnotations = ConverterMetaData.getConverterMethodAnnotations(converterClass, "fromNative", nativeType, FromNativeContext.class);
        this.toNativeMethodAnnotations = ConverterMetaData.getConverterMethodAnnotations(converterClass, "toNative", nativeType, ToNativeContext.class);
        this.toNativeAnnotations = Annotations.mergeAnnotations(this.classAnnotations, this.toNativeMethodAnnotations, this.nativeTypeMethodAnnotations);
        this.fromNativeAnnotations = Annotations.mergeAnnotations(this.classAnnotations, this.fromNativeMethodAnnotations, this.nativeTypeMethodAnnotations);
    }

    private static Collection<Annotation> getToNativeMethodAnnotations(Class converterClass, Class resultClass) {
        try {
            Method baseMethod = converterClass.getMethod("toNative", Object.class, ToNativeContext.class);
            for (Method m : converterClass.getMethods()) {
                Class<?>[] methodParameterTypes;
                if (!m.getName().equals("toNative") || !resultClass.isAssignableFrom(m.getReturnType()) || (methodParameterTypes = m.getParameterTypes()).length != 2 || !methodParameterTypes[1].isAssignableFrom(ToNativeContext.class)) continue;
                return Annotations.mergeAnnotations(Annotations.sortedAnnotationCollection(m.getAnnotations()), Annotations.sortedAnnotationCollection(baseMethod.getAnnotations()));
            }
            return Annotations.EMPTY_ANNOTATIONS;
        }
        catch (SecurityException se) {
            return Annotations.EMPTY_ANNOTATIONS;
        }
        catch (NoSuchMethodException ignored) {
            return Annotations.EMPTY_ANNOTATIONS;
        }
    }

    private static Collection<Annotation> getConverterMethodAnnotations(Class converterClass, String methodName, Class ... parameterClasses) {
        try {
            return Annotations.sortedAnnotationCollection(converterClass.getMethod(methodName, new Class[0]).getAnnotations());
        }
        catch (NoSuchMethodException ignored) {
            return Annotations.EMPTY_ANNOTATIONS;
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static ConverterMetaData getMetaData(Class converterClass, Class nativeType) {
        ConverterMetaData metaData;
        Map<Class, ConverterMetaData> cache;
        Map<Class, ConverterMetaData> map = cache = cacheReference != null ? cacheReference.get() : null;
        if (cache != null && (metaData = cache.get(converterClass)) != null) {
            return metaData;
        }
        return ConverterMetaData.addMetaData(converterClass, nativeType);
    }

    private static synchronized ConverterMetaData addMetaData(Class converterClass, Class nativeType) {
        ConverterMetaData metaData;
        IdentityHashMap cache;
        IdentityHashMap identityHashMap = cache = cacheReference != null ? cacheReference.get() : null;
        if (cache != null && (metaData = cache.get(converterClass)) != null) {
            return metaData;
        }
        HashMap<Class, ConverterMetaData> m = new HashMap<Class, ConverterMetaData>(cache != null ? cache : Collections.EMPTY_MAP);
        metaData = new ConverterMetaData(converterClass, nativeType);
        m.put(converterClass, metaData);
        cache = new IdentityHashMap(m);
        cacheReference = new SoftReference(cache);
        return metaData;
    }

    static Collection<Annotation> getAnnotations(ToNativeConverter toNativeConverter) {
        return toNativeConverter != null ? ConverterMetaData.getMetaData(toNativeConverter.getClass(), toNativeConverter.nativeType()).toNativeAnnotations : Annotations.EMPTY_ANNOTATIONS;
    }

    static Collection<Annotation> getAnnotations(FromNativeConverter fromNativeConverter) {
        return fromNativeConverter != null ? ConverterMetaData.getMetaData(fromNativeConverter.getClass(), fromNativeConverter.nativeType()).fromNativeAnnotations : Annotations.EMPTY_ANNOTATIONS;
    }
}

