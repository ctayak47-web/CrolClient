
package com.kenai.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Type;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallContextCache {
    private final Map<Signature, CallContextRef> contextCache = new ConcurrentHashMap<Signature, CallContextRef>();
    private final ReferenceQueue<CallContext> contextReferenceQueue = new ReferenceQueue();

    public static CallContextCache getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private CallContextCache() {
    }

    public final CallContext getCallContext(Type returnType, Type[] parameterTypes, CallingConvention convention) {
        return this.getCallContext(returnType, parameterTypes, convention, true, false);
    }

    public final CallContext getCallContext(Type returnType, Type[] parameterTypes, CallingConvention convention, boolean saveErrno) {
        return this.getCallContext(returnType, parameterTypes, convention, saveErrno, false);
    }

    public final CallContext getCallContext(Type returnType, int fixedParamCount, Type[] parameterTypes, CallingConvention convention, boolean saveErrno) {
        return this.getCallContext(returnType, fixedParamCount, parameterTypes, convention, saveErrno, false);
    }

    public final CallContext getCallContext(Type returnType, Type[] parameterTypes, CallingConvention convention, boolean saveErrno, boolean faultProtect) {
        return this.getCallContext(returnType, parameterTypes.length, parameterTypes, convention, saveErrno, faultProtect);
    }

    public final CallContext getCallContext(Type returnType, int fixedParamCount, Type[] parameterTypes, CallingConvention convention, boolean saveErrno, boolean faultProtect) {
        CallContext ctx;
        Signature signature = new Signature(returnType, parameterTypes, convention, saveErrno, faultProtect);
        CallContextRef ref = this.contextCache.get(signature);
        if (ref != null && (ctx = (CallContext)ref.get()) != null) {
            return ctx;
        }
        while ((ref = (CallContextRef)this.contextReferenceQueue.poll()) != null) {
            this.contextCache.remove(ref.signature);
        }
        ctx = new CallContext(returnType, fixedParamCount, (Type[])parameterTypes.clone(), convention, saveErrno, faultProtect);
        this.contextCache.put(signature, new CallContextRef(signature, ctx, this.contextReferenceQueue));
        return ctx;
    }

    private static final class SingletonHolder {
        static final CallContextCache INSTANCE = new CallContextCache();

        private SingletonHolder() {
        }
    }

    private static final class Signature {
        private final Type returnType;
        private final Type[] parameterTypes;
        private final CallingConvention convention;
        private final boolean saveErrno;
        private final boolean faultProtect;
        private int hashCode = 0;

        public Signature(Type returnType, Type[] parameterTypes, CallingConvention convention, boolean saveErrno, boolean faultProtect) {
            if (returnType == null || parameterTypes == null) {
                throw new NullPointerException("null return type or parameter types array");
            }
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
            this.convention = convention;
            this.saveErrno = saveErrno;
            this.faultProtect = faultProtect;
        }

        public boolean equals(Object obj) {
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            Signature other = (Signature)obj;
            if (this.convention != other.convention || this.saveErrno != other.saveErrno || this.faultProtect != other.faultProtect) {
                return false;
            }
            if (this.returnType != other.returnType && !this.returnType.equals(other.returnType)) {
                return false;
            }
            if (this.parameterTypes.length == other.parameterTypes.length) {
                for (int i = 0; i < this.parameterTypes.length; ++i) {
                    if (this.parameterTypes[i] == other.parameterTypes[i] || this.parameterTypes[i] != null && this.parameterTypes[i].equals(other.parameterTypes[i])) continue;
                    return false;
                }
                return true;
            }
            return false;
        }

        private final int calculateHashCode() {
            int hash = 7;
            hash = 53 * hash + (this.returnType != null ? this.returnType.hashCode() : 0);
            int paramHash = 1;
            for (int i = 0; i < this.parameterTypes.length; ++i) {
                paramHash = 31 * paramHash + this.parameterTypes[i].hashCode();
            }
            hash = 53 * hash + paramHash;
            hash = 53 * hash + this.convention.hashCode();
            hash = 53 * hash + (this.saveErrno ? 1 : 0);
            hash = 53 * hash + (this.faultProtect ? 1 : 0);
            return hash;
        }

        public int hashCode() {
            return this.hashCode != 0 ? this.hashCode : (this.hashCode = this.calculateHashCode());
        }
    }

    private static final class CallContextRef
    extends SoftReference<CallContext> {
        final Signature signature;

        public CallContextRef(Signature signature, CallContext ctx, ReferenceQueue<CallContext> queue) {
            super(ctx, queue);
            this.signature = signature;
        }
    }
}

