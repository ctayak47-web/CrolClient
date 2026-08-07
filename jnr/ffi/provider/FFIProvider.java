
package jnr.ffi.provider;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Runtime;
import jnr.ffi.provider.InvalidProvider;

public abstract class FFIProvider {
    public static FFIProvider getSystemProvider() {
        return SystemProviderSingletonHolder.INSTANCE;
    }

    protected FFIProvider() {
    }

    public abstract Runtime getRuntime();

    public abstract <T> LibraryLoader<T> createLibraryLoader(Class<T> var1);

    private static FFIProvider newInvalidProvider(String message, Throwable cause) {
        return new InvalidProvider(message, cause);
    }

    private static final class SystemProviderSingletonHolder {
        private static final FFIProvider INSTANCE = SystemProviderSingletonHolder.getInstance();

        private SystemProviderSingletonHolder() {
        }

        static FFIProvider getInstance() {
            String providerName = System.getProperty("jnr.ffi.provider");
            if (providerName == null) {
                Package pkg = FFIProvider.class.getPackage();
                String pkgName = pkg != null && pkg.getName() != null ? pkg.getName() : "jnr.ffi.provider";
                providerName = pkgName + ".jffi.Provider";
            }
            try {
                return (FFIProvider)Class.forName(providerName).newInstance();
            }
            catch (Throwable ex) {
                return FFIProvider.newInvalidProvider("could not load FFI provider " + providerName, ex);
            }
        }
    }
}

