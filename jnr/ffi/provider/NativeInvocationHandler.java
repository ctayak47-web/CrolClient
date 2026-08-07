
package jnr.ffi.provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import jnr.ffi.provider.Invoker;

public class NativeInvocationHandler
implements InvocationHandler {
    private volatile Map<Method, Invoker> fastLookupTable;
    private final Map<Method, Invoker> invokerMap;

    public NativeInvocationHandler(Map<Method, Invoker> invokers) {
        this.invokerMap = invokers;
        this.fastLookupTable = Collections.emptyMap();
    }

    @Override
    public Object invoke(Object self, Method method, Object[] argArray) throws Throwable {
        Invoker invoker = this.fastLookupTable.get(method);
        return invoker != null ? invoker.invoke(self, argArray) : this.lookupAndCacheInvoker(method).invoke(self, argArray);
    }

    private synchronized Invoker lookupAndCacheInvoker(Method method) {
        Invoker invoker = this.fastLookupTable.get(method);
        if (invoker != null) {
            return invoker;
        }
        IdentityHashMap<Method, Invoker> map = new IdentityHashMap<Method, Invoker>(this.fastLookupTable);
        invoker = this.invokerMap.get(method);
        map.put(method, invoker);
        if (invoker == null) {
            throw new UnsatisfiedLinkError("no invoker for native method " + method.getName());
        }
        this.fastLookupTable = map;
        return invoker;
    }
}

