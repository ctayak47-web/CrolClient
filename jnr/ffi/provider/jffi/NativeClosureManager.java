
package jnr.ffi.provider.jffi;

import java.util.IdentityHashMap;
import java.util.Map;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.CachingTypeMapper;
import jnr.ffi.mapper.CompositeTypeMapper;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.jffi.AsmClassLoader;
import jnr.ffi.provider.jffi.ClosureFromNativeConverter;
import jnr.ffi.provider.jffi.ClosureTypeMapper;
import jnr.ffi.provider.jffi.NativeClosureFactory;

final class NativeClosureManager
implements ClosureManager {
    private volatile Map<Class<?>, NativeClosureFactory> factories = new IdentityHashMap();
    private volatile Map<ClassLoader, AsmClassLoader> asmClassLoaders = new IdentityHashMap<ClassLoader, AsmClassLoader>();
    private final Runtime runtime;
    private final SignatureTypeMapper typeMapper;

    NativeClosureManager(Runtime runtime, SignatureTypeMapper typeMapper) {
        this.runtime = runtime;
        this.typeMapper = new CompositeTypeMapper(typeMapper, new CachingTypeMapper(new ClosureTypeMapper()));
    }

    <T> NativeClosureFactory<T> getClosureFactory(Class<T> closureClass) {
        NativeClosureFactory factory = this.factories.get(closureClass);
        if (factory != null) {
            return factory;
        }
        AsmClassLoader asmCl = this.asmClassLoaders.get(closureClass.getClassLoader());
        if (asmCl == null) {
            asmCl = new AsmClassLoader(closureClass.getClassLoader());
            this.asmClassLoaders.put(closureClass.getClassLoader(), asmCl);
        }
        return this.initClosureFactory(closureClass, asmCl);
    }

    @Override
    public <T> T newClosure(Class<? extends T> closureClass, T instance) {
        NativeClosureFactory factory = this.factories.get(closureClass);
        if (factory != null) {
            
        }
        return null;
    }

    @Override
    public final <T> Pointer getClosurePointer(Class<? extends T> closureClass, T instance) {
        return this.getClosureFactory(closureClass).getClosureReference(instance).getPointer();
    }

    synchronized <T> NativeClosureFactory<T> initClosureFactory(Class<T> closureClass, AsmClassLoader classLoader) {
        NativeClosureFactory factory = this.factories.get(closureClass);
        if (factory != null) {
            return factory;
        }
        factory = NativeClosureFactory.newClosureFactory(this.runtime, closureClass, this.typeMapper, classLoader);
        IdentityHashMap factories = new IdentityHashMap();
        factories.putAll(this.factories);
        factories.put(closureClass, factory);
        this.factories = factories;
        return factory;
    }

    <T> ToNativeConverter<T, Pointer> newClosureSite(Class<T> closureClass) {
        return new ClosureSite(this.getClosureFactory(closureClass));
    }

    @ToNativeConverter.NoContext
    public static final class ClosureSite<T>
    implements ToNativeConverter<T, Pointer> {
        private final NativeClosureFactory<T> factory;
        private NativeClosureFactory.ClosureReference closureReference = null;

        private ClosureSite(NativeClosureFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public Pointer toNative(T value, ToNativeContext context) {
            if (value == null) {
                return null;
            }
            if (value instanceof ClosureFromNativeConverter.AbstractClosurePointer) {
                return (ClosureFromNativeConverter.AbstractClosurePointer)value;
            }
            NativeClosureFactory.ClosureReference ref = this.closureReference;
            if (ref != null && ref.getCallable() == value) {
                return ref.getPointer();
            }
            ref = this.factory.getClosureReference(value);
            if (this.closureReference == null || this.closureReference.get() == null) {
                this.closureReference = ref;
            }
            return ref.getPointer();
        }

        @Override
        public Class<Pointer> nativeType() {
            return Pointer.class;
        }
    }
}

