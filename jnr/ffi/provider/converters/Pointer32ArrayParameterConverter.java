
package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class Pointer32ArrayParameterConverter
implements ToNativeConverter<Pointer[], int[]> {
    protected final Runtime runtime;
    protected final int parameterFlags;

    public static ToNativeConverter<Pointer[], int[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return !ParameterFlags.isOut(parameterFlags) ? new Pointer32ArrayParameterConverter(toNativeContext.getRuntime(), parameterFlags) : new Out(toNativeContext.getRuntime(), parameterFlags);
    }

    Pointer32ArrayParameterConverter(Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public Class<int[]> nativeType() {
        return int[].class;
    }

    @Override
    public int[] toNative(Pointer[] pointers, ToNativeContext context) {
        if (pointers == null) {
            return null;
        }
        int[] primitive = new int[pointers.length];
        if (ParameterFlags.isIn(this.parameterFlags)) {
            for (int i = 0; i < pointers.length; ++i) {
                if (pointers[i] != null && !pointers[i].isDirect()) {
                    throw new IllegalArgumentException("invalid pointer in array at index " + i);
                }
                primitive[i] = pointers[i] != null ? (int)pointers[i].address() : 0;
            }
        }
        return primitive;
    }

    public static final class Out
    extends Pointer32ArrayParameterConverter
    implements ToNativeConverter.PostInvocation<Pointer[], int[]> {
        public Out(Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Pointer[] pointers, int[] primitive, ToNativeContext context) {
            if (pointers != null && primitive != null && ParameterFlags.isOut(this.parameterFlags)) {
                MemoryManager mm = this.runtime.getMemoryManager();
                for (int i = 0; i < pointers.length; ++i) {
                    pointers[i] = mm.newPointer(primitive[i]);
                }
            }
        }
    }
}

