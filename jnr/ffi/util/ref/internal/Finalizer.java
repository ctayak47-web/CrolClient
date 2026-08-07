
package jnr.ffi.util.ref.internal;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Finalizer
implements Runnable {
    private static final Logger logger = Logger.getLogger(Finalizer.class.getName());
    private static final String FINALIZABLE_REFERENCE = "jnr.ffi.util.ref.FinalizableReference";
    private Thread thread;
    private final WeakReference<Class<?>> finalizableReferenceClassReference;
    private final PhantomReference<Object> frqReference;
    private final ReferenceQueue<Object> queue = new ReferenceQueue();
    private static final Field inheritableThreadLocals;
    private static final Constructor<Thread> inheritableThreadlocalsConstructor;

    public static ReferenceQueue<Object> startFinalizer(Class<?> finalizableReferenceClass, Object frq) {
        if (!finalizableReferenceClass.getName().equals(FINALIZABLE_REFERENCE)) {
            throw new IllegalArgumentException("Expected jnr.ffi.util.ref.FinalizableReference.");
        }
        Finalizer finalizer = new Finalizer(finalizableReferenceClass, frq);
        finalizer.start();
        return finalizer.queue;
    }

    private Finalizer(Class<?> finalizableReferenceClass, Object frq) {
        this.finalizableReferenceClassReference = new WeakReference(finalizableReferenceClass);
        this.frqReference = new PhantomReference<Object>(frq, this.queue);
    }

    public void start() {
        if (inheritableThreadlocalsConstructor != null) {
            try {
                this.thread = inheritableThreadlocalsConstructor.newInstance(Thread.currentThread().getThreadGroup(), this, Finalizer.class.getName(), 0, false);
            }
            catch (Throwable t) {
                logger.log(Level.INFO, "Failed to disable thread local values inherited by reference finalizer thread.", t);
            }
        }
        if (this.thread == null) {
            this.thread = new Thread(this, Finalizer.class.getName());
            if (inheritableThreadLocals != null) {
                try {
                    inheritableThreadLocals.set(this.thread, null);
                }
                catch (Throwable t) {
                    logger.log(Level.INFO, "Failed to clear thread local values inherited by reference finalizer thread.", t);
                }
            }
        }
        this.thread.setDaemon(true);
        this.thread.setPriority(10);
        this.thread.setContextClassLoader(null);
        this.thread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                while (this.cleanUp(this.queue.remove())) {
                }
            }
            catch (InterruptedException interruptedException) {
                continue;
            }
            break;
        }
    }

    private boolean cleanUp(Reference<?> reference) {
        Method finalizeReferentMethod = this.getFinalizeReferentMethod();
        if (finalizeReferentMethod == null) {
            return false;
        }
        do {
            reference.clear();
            if (reference == this.frqReference) {
                return false;
            }
            try {
                finalizeReferentMethod.invoke(reference, new Object[0]);
            }
            catch (Throwable t) {
                logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
            }
        } while ((reference = this.queue.poll()) != null);
        return true;
    }

    private Method getFinalizeReferentMethod() {
        Class finalizableReferenceClass = (Class)this.finalizableReferenceClassReference.get();
        if (finalizableReferenceClass == null) {
            return null;
        }
        try {
            return finalizableReferenceClass.getMethod("finalizeReferent", new Class[0]);
        }
        catch (NoSuchMethodException e) {
            throw new AssertionError((Object)e);
        }
    }

    public static Field getInheritableThreadLocalsField() {
        try {
            Field inheritableThreadLocals = Thread.class.getDeclaredField("inheritableThreadLocals");
            inheritableThreadLocals.setAccessible(true);
            return inheritableThreadLocals;
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static Constructor<Thread> getInheritableThreadLocalsConstructor() {
        try {
            return Thread.class.getConstructor(ThreadGroup.class, Runnable.class, String.class, Long.TYPE, Boolean.TYPE);
        }
        catch (Throwable t) {
            return null;
        }
    }

    static {
        Constructor<Thread> itlc = null;
        try {
            itlc = Finalizer.getInheritableThreadLocalsConstructor();
        }
        catch (Throwable throwable) {
            
        }
        Field itl = null;
        if (itlc == null) {
            try {
                itl = Finalizer.getInheritableThreadLocalsField();
            }
            catch (Throwable throwable) {
                
            }
        }
        inheritableThreadLocals = itl;
        inheritableThreadlocalsConstructor = itlc;
        if (itl == null && itlc == null) {
            logger.log(Level.INFO, "Couldn't access Thread.inheritableThreadLocals or appropriate constructor. Reference finalizer threads will inherit thread local values.");
        }
    }
}

