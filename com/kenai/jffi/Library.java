
package com.kenai.jffi;

import com.kenai.jffi.Foreign;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public final class Library {
    private static final Map<String, WeakReference<Library>> cache = new ConcurrentHashMap<String, WeakReference<Library>>();
    private static final Object lock = new Object();
    private static final ThreadLocal<String> lastError = new ThreadLocal();
    public static final int LAZY = 1;
    public static final int NOW = 2;
    public static final int LOCAL = 4;
    public static final int GLOBAL = 8;
    private final long handle;
    private final String name;
    private final Foreign foreign;
    private volatile int disposed;
    private static final AtomicIntegerFieldUpdater<Library> UPDATER = AtomicIntegerFieldUpdater.newUpdater(Library.class, "disposed");

    private static long dlopen(Foreign foreign, String name, int flags) {
        try {
            return Foreign.dlopen(name, flags);
        }
        catch (UnsatisfiedLinkError ex) {
            lastError.set(ex.getMessage());
            return 0L;
        }
    }

    public static final Library getDefault() {
        return DefaultLibrary.INSTANCE;
    }

    public static final Library getCachedInstance(String name, int flags) {
        Library lib;
        if (name == null) {
            return Library.getDefault();
        }
        WeakReference<Library> ref = cache.get(name);
        Library library = lib = ref != null ? (Library)ref.get() : null;
        if (lib != null) {
            return lib;
        }
        lib = Library.openLibrary(name, flags);
        if (lib == null) {
            return null;
        }
        cache.put(name, new WeakReference<Library>(lib));
        return lib;
    }

    public static final Library openLibrary(String name, int flags) {
        Foreign foreign;
        long address;
        if (flags == 0) {
            flags = 5;
        }
        return (address = Library.dlopen(foreign = Foreign.getInstance(), name, flags)) != 0L ? new Library(foreign, name, address) : null;
    }

    private Library(Foreign foreign, String name, long address) {
        this.foreign = foreign;
        this.name = name;
        this.handle = address;
    }

    public final long getSymbolAddress(String name) {
        try {
            return Foreign.dlsym(this.handle, name);
        }
        catch (UnsatisfiedLinkError ex) {
            Library library = this;
            lastError.set(library.foreign.dlerror());
            return 0L;
        }
    }

    public static final String getLastError() {
        String error = lastError.get();
        return error != null ? error : "unknown";
    }

    protected void finalize() throws Throwable {
        try {
            int disposed = UPDATER.getAndSet(this, 1);
            if (disposed == 0 && this.handle != 0L) {
                Foreign.dlclose(this.handle);
            }
        }
        finally {
            super.finalize();
        }
    }

    private static final class DefaultLibrary {
        private static final Library INSTANCE = Library.openLibrary(null, 9);

        private DefaultLibrary() {
        }
    }
}

