
package jnr.ffi.provider.jffi;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.Buffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import jnr.ffi.Address;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Type;
import jnr.ffi.annotations.TypeDefinition;

class Types {
    private static Reference<Map<Class, Map<Collection<Annotation>, Type>>> typeCacheReference;

    Types() {
    }

    static Type getType(Runtime runtime, Class javaType, Collection<Annotation> annotations) {
        Map<Class, Map<Collection<Annotation>, Type>> cache = typeCacheReference != null ? typeCacheReference.get() : null;
        Map<Collection<Annotation>, Type> aliasCache = cache != null ? cache.get(javaType) : null;
        Type type = aliasCache != null ? aliasCache.get(annotations) : null;
        return type != null ? type : Types.lookupAndCacheType(runtime, javaType, annotations);
    }

    private static synchronized Type lookupAndCacheType(Runtime runtime, Class javaType, Collection<Annotation> annotations) {
        Type type;
        HashMap cache = typeCacheReference != null ? typeCacheReference.get() : null;
        HashMap<Collection<Annotation>, Type> aliasCache = cache != null ? cache.get(javaType) : null;
        Type type2 = type = aliasCache != null ? aliasCache.get(annotations) : null;
        if (type != null) {
            return type;
        }
        cache = new HashMap(cache != null ? cache : Collections.EMPTY_MAP);
        aliasCache = new HashMap<Collection<Annotation>, Type>(aliasCache != null ? aliasCache : Collections.EMPTY_MAP);
        type = Types.lookupType(runtime, javaType, annotations);
        aliasCache.put(annotations, type);
        cache.put(javaType, Collections.unmodifiableMap(aliasCache));
        typeCacheReference = new SoftReference(Collections.unmodifiableMap(new IdentityHashMap(cache)));
        return type;
    }

    private static Type lookupAliasedType(Runtime runtime, Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            TypeDefinition typedef = a.annotationType().getAnnotation(TypeDefinition.class);
            if (typedef == null) continue;
            return runtime.findType(typedef.alias());
        }
        return null;
    }

    static Type lookupType(Runtime runtime, Class type, Collection<Annotation> annotations) {
        Type aliasedType;
        Type type2 = aliasedType = type.isArray() ? null : Types.lookupAliasedType(runtime, annotations);
        if (aliasedType != null) {
            return aliasedType;
        }
        if (Void.class.isAssignableFrom(type) || Void.TYPE == type) {
            return runtime.findType(NativeType.VOID);
        }
        if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE == type) {
            return runtime.findType(NativeType.SINT);
        }
        if (Byte.class.isAssignableFrom(type) || Byte.TYPE == type) {
            return runtime.findType(NativeType.SCHAR);
        }
        if (Short.class.isAssignableFrom(type) || Short.TYPE == type) {
            return runtime.findType(NativeType.SSHORT);
        }
        if (Integer.class.isAssignableFrom(type) || Integer.TYPE == type) {
            return runtime.findType(NativeType.SINT);
        }
        if (Long.class.isAssignableFrom(type) || Long.TYPE == type) {
            return runtime.findType(NativeType.SLONG);
        }
        if (Float.class.isAssignableFrom(type) || Float.TYPE == type) {
            return runtime.findType(NativeType.FLOAT);
        }
        if (Double.class.isAssignableFrom(type) || Double.TYPE == type) {
            return runtime.findType(NativeType.DOUBLE);
        }
        if (Pointer.class.isAssignableFrom(type)) {
            return runtime.findType(NativeType.ADDRESS);
        }
        if (Address.class.isAssignableFrom(type)) {
            return runtime.findType(NativeType.ADDRESS);
        }
        if (Buffer.class.isAssignableFrom(type)) {
            return runtime.findType(NativeType.ADDRESS);
        }
        if (CharSequence.class.isAssignableFrom(type)) {
            return runtime.findType(NativeType.ADDRESS);
        }
        if (type.isArray()) {
            return runtime.findType(NativeType.ADDRESS);
        }
        throw new IllegalArgumentException("unsupported type: " + type);
    }
}

