
package jnr.ffi.provider.jffi;

import java.util.Map;
import jnr.ffi.LibraryOption;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.CachingTypeMapper;
import jnr.ffi.mapper.CompositeTypeMapper;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.SignatureTypeMapperAdapter;
import jnr.ffi.mapper.TypeMapper;
import jnr.ffi.provider.NullTypeMapper;
import jnr.ffi.provider.jffi.AnnotationTypeMapper;
import jnr.ffi.provider.jffi.AsmClassLoader;
import jnr.ffi.provider.jffi.InvokerTypeMapper;
import jnr.ffi.provider.jffi.NativeClosureManager;
import jnr.ffi.provider.jffi.NativeLibrary;
import jnr.ffi.provider.jffi.NativeLibraryLoader;

public abstract class LibraryLoader {
    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static SignatureTypeMapper getSignatureTypeMapper(Map<LibraryOption, ?> libraryOptions) {
        void var1_6;
        if (libraryOptions.containsKey((Object)LibraryOption.TypeMapper)) {
            Object tm = libraryOptions.get((Object)LibraryOption.TypeMapper);
            if (tm instanceof SignatureTypeMapper) {
                SignatureTypeMapper signatureTypeMapper = (SignatureTypeMapper)tm;
                return var1_6;
            } else {
                if (!(tm instanceof TypeMapper)) throw new IllegalArgumentException("TypeMapper option is not a valid TypeMapper instance");
                SignatureTypeMapperAdapter signatureTypeMapperAdapter = new SignatureTypeMapperAdapter((TypeMapper)tm);
            }
            return var1_6;
        } else {
            NullTypeMapper nullTypeMapper = new NullTypeMapper();
        }
        return var1_6;
    }

    static CompositeTypeMapper newCompositeTypeMapper(Runtime runtime, AsmClassLoader classLoader, SignatureTypeMapper typeMapper, CompositeTypeMapper closureTypeMapper) {
        return new CompositeTypeMapper(typeMapper, new CachingTypeMapper(new InvokerTypeMapper(new NativeClosureManager(runtime, closureTypeMapper), classLoader, NativeLibraryLoader.ASM_ENABLED)), new CachingTypeMapper(new AnnotationTypeMapper()));
    }

    static CompositeTypeMapper newClosureTypeMapper(AsmClassLoader classLoader, SignatureTypeMapper typeMapper) {
        return new CompositeTypeMapper(typeMapper, new CachingTypeMapper(new InvokerTypeMapper(null, classLoader, NativeLibraryLoader.ASM_ENABLED)), new CachingTypeMapper(new AnnotationTypeMapper()));
    }

    abstract <T> T loadLibrary(NativeLibrary var1, Class<T> var2, Map<LibraryOption, ?> var3, boolean var4);
}

