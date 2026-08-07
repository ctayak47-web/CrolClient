
package jnr.ffi.util.ref;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnr.ffi.util.ref.FinalizableReference;

public class FinalizableReferenceQueue {
    private static final Logger logger = Logger.getLogger(FinalizableReferenceQueue.class.getName());
    private static final String FINALIZER_CLASS_NAME = "jnr.ffi.util.ref.internal.Finalizer";
    private static final Method startFinalizer;
    private static final Map<FinalizableReferenceQueue, Boolean> finalizerQueues;
    final ReferenceQueue<Object> queue;
    final boolean threadStarted;

    public FinalizableReferenceQueue() {
        ReferenceQueue queue;
        boolean threadStarted = false;
        try {
            queue = (ReferenceQueue)startFinalizer.invoke(null, FinalizableReference.class, this);
            threadStarted = true;
        }
        catch (IllegalAccessException impossible) {
            throw new AssertionError((Object)impossible);
        }
        catch (Throwable t) {
            logger.log(Level.INFO, "Failed to start reference finalizer thread. Reference cleanup will only occur when new references are created.", t);
            queue = new ReferenceQueue();
        }
        this.queue = queue;
        this.threadStarted = threadStarted;
        finalizerQueues.put(this, Boolean.TRUE);
    }

    void cleanUp() {
        if (!this.threadStarted) {
            this.pollReferenceQueue();
        }
    }

    private void pollReferenceQueue() {
        Reference<Object> reference;
        while ((reference = this.queue.poll()) != null) {
            reference.clear();
            try {
                ((FinalizableReference)((Object)reference)).finalizeReferent();
            }
            catch (Throwable t) {
                logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
            }
        }
    }

    private static Class<?> loadFinalizer(FinalizerLoader ... loaders) {
        for (FinalizerLoader loader : loaders) {
            Class<?> finalizer = loader.loadFinalizer();
            if (finalizer == null) continue;
            return finalizer;
        }
        throw new AssertionError();
    }

    static Method getStartFinalizer(Class<?> finalizer) {
        try {
            return finalizer.getMethod("startFinalizer", Class.class, Object.class);
        }
        catch (NoSuchMethodException e) {
            throw new AssertionError((Object)e);
        }
    }

    public static void cleanUpAll() {
        try {
            for (Object frq : finalizerQueues.keySet().toArray()) {
                ((FinalizableReferenceQueue)frq).cleanUp();
            }
        }
        catch (Throwable throwable) {
            
        }
    }

    static {
        Class<?> finalizer = FinalizableReferenceQueue.loadFinalizer(new SystemLoader(), new DecoupledLoader(), new DirectLoader());
        startFinalizer = FinalizableReferenceQueue.getStartFinalizer(finalizer);
        finalizerQueues = Collections.synchronizedMap(new WeakHashMap());
    }

    static interface FinalizerLoader {
        public Class<?> loadFinalizer();
    }

    static class SystemLoader
    implements FinalizerLoader {
        SystemLoader() {
        }

        @Override
        public Class<?> loadFinalizer() {
            ClassLoader systemLoader;
            try {
                systemLoader = ClassLoader.getSystemClassLoader();
            }
            catch (SecurityException e) {
                logger.info("Not allowed to access system class loader.");
                return null;
            }
            if (systemLoader != null) {
                try {
                    return systemLoader.loadClass(FinalizableReferenceQueue.FINALIZER_CLASS_NAME);
                }
                catch (ClassNotFoundException e) {
                    return null;
                }
            }
            return null;
        }
    }

    static class DecoupledLoader
    implements FinalizerLoader {
        private static final String LOADING_ERROR = "Could not load Finalizer in its own class loader.Loading Finalizer in the current class loader instead. As a result, you will not be ableto garbage collect this class loader. To support reclaiming this class loader, eitherresolve the underlying issue, or move Google Collections to your system class path.";

        DecoupledLoader() {
        }

        @Override
        public Class<?> loadFinalizer() {
            try {
                URLClassLoader finalizerLoader = this.newLoader(this.getBaseUrl());
                return finalizerLoader.loadClass(FinalizableReferenceQueue.FINALIZER_CLASS_NAME);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, LOADING_ERROR, e);
                return null;
            }
        }

        URL getBaseUrl() throws IOException {
            String finalizerPath = FinalizableReferenceQueue.FINALIZER_CLASS_NAME.replace('.', '/') + ".class";
            URL finalizerUrl = this.getClass().getClassLoader().getResource(finalizerPath);
            if (finalizerUrl == null) {
                throw new FileNotFoundException(finalizerPath);
            }
            String urlString = finalizerUrl.toString();
            if (!urlString.endsWith(finalizerPath)) {
                throw new IOException("Unsupported path style: " + urlString);
            }
            urlString = urlString.substring(0, urlString.length() - finalizerPath.length());
            return new URL(finalizerUrl, urlString);
        }

        URLClassLoader newLoader(URL base) {
            return new URLClassLoader(new URL[]{base});
        }
    }

    static class DirectLoader
    implements FinalizerLoader {
        DirectLoader() {
        }

        @Override
        public Class<?> loadFinalizer() {
            try {
                return Class.forName(FinalizableReferenceQueue.FINALIZER_CLASS_NAME);
            }
            catch (ClassNotFoundException e) {
                throw new AssertionError((Object)e);
            }
        }
    }
}

