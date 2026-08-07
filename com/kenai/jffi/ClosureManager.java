
package com.kenai.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.CallContextCache;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Closure;
import com.kenai.jffi.ClosureMagazine;
import com.kenai.jffi.ClosurePool;
import com.kenai.jffi.Foreign;
import com.kenai.jffi.Type;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

public final class ClosureManager {
    private final Map<CallContext, Reference<ClosurePool>> poolMap = new WeakHashMap<CallContext, Reference<ClosurePool>>();

    public static ClosureManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private ClosureManager() {
    }

    public final Closure.Handle newClosure(Closure closure, Type returnType, Type[] parameterTypes, CallingConvention convention) {
        return this.newClosure(closure, CallContextCache.getInstance().getCallContext(returnType, parameterTypes, convention));
    }

    public final Closure.Handle newClosure(Closure closure, CallContext callContext) {
        ClosurePool pool = this.getClosurePool(callContext);
        return pool.newClosureHandle(closure);
    }

    public final synchronized ClosurePool getClosurePool(CallContext callContext) {
        ClosurePool pool;
        Reference<ClosurePool> ref = this.poolMap.get(callContext);
        if (ref != null && (pool = ref.get()) != null) {
            return pool;
        }
        pool = new ClosurePool(callContext);
        this.poolMap.put(callContext, new SoftReference<ClosurePool>(pool));
        return pool;
    }

    public ClosureMagazine newClosureMagazine(CallContext callContext, Method method) {
        Foreign foreign = Foreign.getInstance();
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        boolean callWithPrimitiveArgs = methodParameterTypes.length < 1 || !Closure.Buffer.class.isAssignableFrom(method.getParameterTypes()[0]);
        long magazine = foreign.newClosureMagazine(callContext.getAddress(), method, callWithPrimitiveArgs);
        if (magazine == 0L) {
            throw new RuntimeException("could not allocate new closure magazine");
        }
        return new ClosureMagazine(foreign, callContext, magazine);
    }

    private static final class SingletonHolder {
        static final ClosureManager INSTANCE = new ClosureManager();

        private SingletonHolder() {
        }
    }
}

