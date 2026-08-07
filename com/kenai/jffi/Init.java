
package com.kenai.jffi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class Init {
    private static volatile boolean loaded = false;
    static final String stubLoaderClassName = Init.class.getPackage().getName() + ".internal.StubLoader";

    private Init() {
    }

    static void load() {
        if (loaded) {
            return;
        }
        ArrayList<ReflectiveOperationException> failureCauses = new ArrayList<ReflectiveOperationException>();
        List<ClassLoader> loaders = Init.getClassLoaders();
        for (ClassLoader cl : loaders) {
            try {
                Class<?> c = Class.forName(stubLoaderClassName, true, cl);
                Method method = c.getDeclaredMethod("isLoaded", new Class[0]);
                if (loaded |= ((Boolean)Boolean.class.cast(method.invoke(c, new Object[0]))).booleanValue()) continue;
                Method getFailureCause = c.getDeclaredMethod("getFailureCause", new Class[0]);
                throw (Throwable)Throwable.class.cast(getFailureCause.invoke(c, new Object[0]));
            }
            catch (IllegalAccessException ex) {
                failureCauses.add(ex);
            }
            catch (InvocationTargetException ex) {
                failureCauses.add(ex);
            }
            catch (ClassNotFoundException ex) {
                failureCauses.add(ex);
            }
            catch (Throwable throwable) {
                if (throwable instanceof UnsatisfiedLinkError) {
                    throw (UnsatisfiedLinkError)throwable;
                }
                throw Init.newLoadError(throwable);
            }
        }
        if (!loaded && !failureCauses.isEmpty()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            for (Throwable throwable : failureCauses) {
                throwable.printStackTrace(pw);
            }
            throw new UnsatisfiedLinkError(sw.toString());
        }
    }

    private static List<ClassLoader> getClassLoaders() {
        ArrayList<ClassLoader> loaders = new ArrayList<ClassLoader>();
        try {
            loaders.add(ClassLoader.getSystemClassLoader());
        }
        catch (SecurityException securityException) {
            
        }
        try {
            loaders.add(Thread.currentThread().getContextClassLoader());
        }
        catch (SecurityException securityException) {
            
        }
        loaders.add(Init.class.getClassLoader());
        int nullCount = 0;
        Iterator it = loaders.iterator();
        while (it.hasNext()) {
            if (it.next() != null || ++nullCount <= 1) continue;
            it.remove();
        }
        return Collections.unmodifiableList(loaders);
    }

    private static UnsatisfiedLinkError newLoadError(Throwable cause) {
        UnsatisfiedLinkError error = new UnsatisfiedLinkError(cause.getLocalizedMessage());
        error.initCause(cause);
        return error;
    }
}

