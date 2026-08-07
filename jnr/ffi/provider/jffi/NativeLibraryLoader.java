
package jnr.ffi.provider.jffi;

import java.util.Collection;
import java.util.Map;
import jnr.ffi.LibraryLoader;
import jnr.ffi.LibraryOption;
import jnr.ffi.provider.jffi.AsmLibraryLoader;
import jnr.ffi.provider.jffi.NativeLibrary;
import jnr.ffi.provider.jffi.ReflectionLibraryLoader;
import jnr.ffi.provider.jffi.Util;

class NativeLibraryLoader<T>
extends LibraryLoader<T> {
    static final boolean ASM_ENABLED = Util.getBooleanProperty("jnr.ffi.asm.enabled", true);

    NativeLibraryLoader(Class<T> interfaceClass) {
        super(interfaceClass);
    }

    @Override
    public T loadLibrary(Class<T> interfaceClass, Collection<String> libraryNames, Collection<String> searchPaths, Map<LibraryOption, Object> options, boolean failImmediately) {
        NativeLibrary nativeLibrary = new NativeLibrary(libraryNames, searchPaths, options);
        try {
            return ASM_ENABLED ? new AsmLibraryLoader().loadLibrary(nativeLibrary, interfaceClass, options, failImmediately) : new ReflectionLibraryLoader().loadLibrary(nativeLibrary, interfaceClass, options, failImmediately);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

