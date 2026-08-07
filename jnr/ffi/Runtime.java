
package jnr.ffi;

import java.nio.ByteOrder;
import java.util.List;
import jnr.ffi.NativeType;
import jnr.ffi.ObjectReferenceManager;
import jnr.ffi.Type;
import jnr.ffi.TypeAlias;
import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.FFIProvider;
import jnr.ffi.provider.LoadedLibrary;
import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.jffi.NativeLibrary;
import jnr.ffi.provider.jffi.NativeRuntime;

public abstract class Runtime {
    public static Runtime getSystemRuntime() {
        return SingletonHolder.SYSTEM_RUNTIME;
    }

    public static Runtime getRuntime(Object library) {
        return ((LoadedLibrary)library).getRuntime();
    }

    public static List<NativeLibrary.LoadedLibraryData> getLoadedLibraries() {
        return NativeRuntime.getLoadedLibraries();
    }

    public abstract Type findType(NativeType var1);

    public abstract Type findType(TypeAlias var1);

    public abstract MemoryManager getMemoryManager();

    public abstract ClosureManager getClosureManager();

    public abstract <T> ObjectReferenceManager<T> newObjectReferenceManager();

    public abstract int getLastError();

    public abstract void setLastError(int var1);

    public abstract long addressMask();

    public abstract int addressSize();

    public abstract int longSize();

    public abstract ByteOrder byteOrder();

    public abstract boolean isCompatible(Runtime var1);

    private static final class SingletonHolder {
        public static final Runtime SYSTEM_RUNTIME = FFIProvider.getSystemProvider().getRuntime();

        private SingletonHolder() {
        }
    }
}

