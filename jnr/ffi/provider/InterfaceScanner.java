
package jnr.ffi.provider;

import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import jnr.ffi.CallingConvention;
import jnr.ffi.Variable;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.provider.NativeFunction;
import jnr.ffi.provider.NativeVariable;

public class InterfaceScanner {
    private final Class interfaceClass;
    private final SignatureTypeMapper typeMapper;
    private final CallingConvention callingConvention;
    private final Method[] methods;
    private static final Method methodIsDefault;

    public InterfaceScanner(Class interfaceClass, SignatureTypeMapper typeMapper, CallingConvention callingConvention) {
        this.interfaceClass = interfaceClass;
        this.typeMapper = typeMapper;
        this.methods = interfaceClass.getMethods();
        this.callingConvention = interfaceClass.isAnnotationPresent(StdCall.class) ? CallingConvention.STDCALL : callingConvention;
    }

    public Collection<NativeFunction> functions() {
        return new AbstractCollection<NativeFunction>(){

            @Override
            public Iterator<NativeFunction> iterator() {
                return new FunctionsIterator(InterfaceScanner.this.methods);
            }

            @Override
            public int size() {
                return 0;
            }
        };
    }

    public Collection<NativeVariable> variables() {
        return new AbstractCollection<NativeVariable>(){

            @Override
            public Iterator<NativeVariable> iterator() {
                return new VariablesIterator(InterfaceScanner.this.methods);
            }

            @Override
            public int size() {
                return 0;
            }
        };
    }

    private static boolean isDefault(Method method) {
        if (methodIsDefault == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(methodIsDefault.invoke((Object)method, new Object[0]));
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected error attempting to call isDefault method", e);
        }
    }

    static {
        Method isDefault = null;
        try {
            isDefault = Method.class.getMethod("isDefault", null);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            
        }
        methodIsDefault = isDefault;
    }

    private final class VariablesIterator
    implements Iterator<NativeVariable> {
        private final Method[] methods;
        private int nextIndex;

        private VariablesIterator(Method[] methods) {
            this.methods = methods;
            this.nextIndex = 0;
        }

        @Override
        public boolean hasNext() {
            while (this.nextIndex < this.methods.length) {
                if (Variable.class == this.methods[this.nextIndex].getReturnType()) {
                    return true;
                }
                ++this.nextIndex;
            }
            return false;
        }

        @Override
        public NativeVariable next() {
            return new NativeVariable(this.methods[this.nextIndex++]);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class FunctionsIterator
    implements Iterator<NativeFunction> {
        private final Method[] methods;
        private int nextIndex;

        private FunctionsIterator(Method[] methods) {
            this.methods = methods;
            this.nextIndex = 0;
        }

        @Override
        public boolean hasNext() {
            while (this.nextIndex < this.methods.length) {
                if (!Variable.class.isAssignableFrom(this.methods[this.nextIndex].getReturnType()) && !InterfaceScanner.isDefault(this.methods[this.nextIndex])) {
                    return true;
                }
                ++this.nextIndex;
            }
            return false;
        }

        @Override
        public NativeFunction next() {
            CallingConvention callingConvention = this.methods[this.nextIndex].isAnnotationPresent(StdCall.class) ? CallingConvention.STDCALL : InterfaceScanner.this.callingConvention;
            return new NativeFunction(this.methods[this.nextIndex++], callingConvention);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

