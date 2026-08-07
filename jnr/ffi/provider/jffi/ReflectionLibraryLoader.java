
package jnr.ffi.provider.jffi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import jnr.ffi.CallingConvention;
import jnr.ffi.LibraryOption;
import jnr.ffi.Runtime;
import jnr.ffi.Variable;
import jnr.ffi.annotations.Synchronized;
import jnr.ffi.mapper.CompositeTypeMapper;
import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.provider.IdentityFunctionMapper;
import jnr.ffi.provider.InterfaceScanner;
import jnr.ffi.provider.Invoker;
import jnr.ffi.provider.LoadedLibrary;
import jnr.ffi.provider.NativeFunction;
import jnr.ffi.provider.NativeInvocationHandler;
import jnr.ffi.provider.NativeVariable;
import jnr.ffi.provider.jffi.AsmClassLoader;
import jnr.ffi.provider.jffi.DefaultInvokerFactory;
import jnr.ffi.provider.jffi.InvokerUtil;
import jnr.ffi.provider.jffi.LibraryLoader;
import jnr.ffi.provider.jffi.NativeFunctionMapperContext;
import jnr.ffi.provider.jffi.NativeLibrary;
import jnr.ffi.provider.jffi.NativeRuntime;
import jnr.ffi.provider.jffi.ReflectionVariableAccessorGenerator;
import jnr.ffi.util.Annotations;

class ReflectionLibraryLoader
extends LibraryLoader {
    ReflectionLibraryLoader() {
    }

    @Override
    <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, boolean failImmediately) {
        LazyLoader invokers = new LazyLoader(library, interfaceClass, libraryOptions);
        if (failImmediately) {
            SignatureTypeMapper typeMapper = ReflectionLibraryLoader.getSignatureTypeMapper(libraryOptions);
            CallingConvention libraryCallingConvention = InvokerUtil.getCallingConvention(interfaceClass, libraryOptions);
            InterfaceScanner scanner = new InterfaceScanner(interfaceClass, typeMapper, libraryCallingConvention);
            for (NativeFunction function : scanner.functions()) {
                invokers.get(function.getMethod());
            }
            for (NativeVariable variable : scanner.variables()) {
                invokers.get(variable.getMethod());
            }
        }
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass, LoadedLibrary.class}, new NativeInvocationHandler(invokers)));
    }

    private static final class LazyLoader<T>
    extends AbstractMap<Method, Invoker> {
        private final DefaultInvokerFactory invokerFactory;
        private final Runtime runtime = NativeRuntime.getInstance();
        private final AsmClassLoader classLoader = new AsmClassLoader();
        private final SignatureTypeMapper typeMapper;
        private final FunctionMapper functionMapper;
        private final CallingConvention libraryCallingConvention;
        private final boolean libraryIsSynchronized;
        private final NativeLibrary library;
        private final Class<T> interfaceClass;
        private final Map<LibraryOption, ?> libraryOptions;

        private LazyLoader(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
            this.library = library;
            this.interfaceClass = interfaceClass;
            this.libraryOptions = libraryOptions;
            this.functionMapper = libraryOptions.containsKey((Object)LibraryOption.FunctionMapper) ? (FunctionMapper)libraryOptions.get((Object)LibraryOption.FunctionMapper) : IdentityFunctionMapper.getInstance();
            SignatureTypeMapper typeMapper = LibraryLoader.getSignatureTypeMapper(libraryOptions);
            CompositeTypeMapper closureTypeMapper = LibraryLoader.newClosureTypeMapper(this.classLoader, typeMapper);
            this.typeMapper = LibraryLoader.newCompositeTypeMapper(this.runtime, this.classLoader, typeMapper, closureTypeMapper);
            this.libraryCallingConvention = InvokerUtil.getCallingConvention(interfaceClass, libraryOptions);
            this.libraryIsSynchronized = interfaceClass.isAnnotationPresent(Synchronized.class);
            this.invokerFactory = new DefaultInvokerFactory(this.runtime, library, this.typeMapper, this.functionMapper, this.libraryCallingConvention, libraryOptions, this.libraryIsSynchronized);
        }

        @Override
        public Set<Map.Entry<Method, Invoker>> entrySet() {
            throw new UnsupportedOperationException("not implemented");
        }

        @Override
        public synchronized Invoker get(Object key) {
            if (!(key instanceof Method)) {
                throw new IllegalArgumentException("key not instance of Method");
            }
            Method method = (Method)key;
            if (Variable.class.isAssignableFrom(method.getReturnType())) {
                return this.getVariableAccessor(method);
            }
            if (method.getName().equals("getRuntime") && method.getReturnType().isAssignableFrom(NativeRuntime.class)) {
                return new GetRuntimeInvoker(this.runtime);
            }
            return this.invokerFactory.createInvoker(method);
        }

        private Invoker getVariableAccessor(Method method) {
            Collection<Annotation> annotations = Annotations.sortedAnnotationCollection(method.getAnnotations());
            String functionName = this.functionMapper.mapFunctionName(method.getName(), new NativeFunctionMapperContext(this.library, annotations));
            long symbolAddress = this.library.getSymbolAddress(functionName);
            if (symbolAddress == 0L) {
                return new FunctionNotFoundInvoker(method, functionName);
            }
            Variable variable = ReflectionVariableAccessorGenerator.createVariableAccessor(this.runtime, method, symbolAddress, this.typeMapper, annotations);
            return new VariableAcccessorInvoker(variable);
        }

        private static final class VariableAcccessorInvoker
        implements Invoker {
            private final Variable variable;

            private VariableAcccessorInvoker(Variable variable) {
                this.variable = variable;
            }

            @Override
            public Object invoke(Object self, Object[] parameters) {
                return this.variable;
            }
        }
    }

    private static final class GetRuntimeInvoker
    implements Invoker {
        private final Runtime runtime;

        private GetRuntimeInvoker(Runtime runtime) {
            this.runtime = runtime;
        }

        @Override
        public Object invoke(Object self, Object[] parameters) {
            return this.runtime;
        }
    }

    private static final class FunctionNotFoundInvoker
    implements Invoker {
        private final Method method;
        private final String functionName;

        private FunctionNotFoundInvoker(Method method, String functionName) {
            this.method = method;
            this.functionName = functionName;
        }

        @Override
        public Object invoke(Object self, Object[] parameters) {
            throw new UnsatisfiedLinkError(String.format("native method '%s' not found for method %s", this.functionName, this.method));
        }
    }
}

