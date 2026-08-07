
package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class Pointer64ArrayParameterConverter
implements ToNativeConverter<Pointer[], long[]> {
    protected final Runtime runtime;
    protected final int parameterFlags;

    public static ToNativeConverter<Pointer[], long[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return !ParameterFlags.isOut(parameterFlags) ? new Pointer64ArrayParameterConverter(toNativeContext.getRuntime(), parameterFlags) : new Out(toNativeContext.getRuntime(), parameterFlags);
    }

    Pointer64ArrayParameterConverter(Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    @LongLong
    public Class<long[]> nativeType() {
        return long[].class;
    }

    @Override
    public long[] toNative(Pointer[] pointers, ToNativeContext context) {
        if (pointers == null) {
            return null;
        }
        long[] primitive = new long[pointers.length];
        if (ParameterFlags.isIn(this.parameterFlags)) {
            for (int i = 0; i < pointers.length; ++i) {
                if (pointers[i] != null && !pointers[i].isDirect()) {
                    throw new IllegalArgumentException("invalid pointer in array at index " + i);
                }
                primitive[i] = pointers[i] != null ? pointers[i].address() : 0L;
            }
        }
        return primitive;
    }

    public static final class Out
    extends Pointer64ArrayParameterConverter
    implements ToNativeConverter.PostInvocation<Pointer[], long[]> {
        Out(Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Pointer[] pointers, long[] primitive, ToNativeContext context) {
            if (pointers != null && primitive != null) {
                MemoryManager mm = this.runtime.getMemoryManager();
                for (int i = 0; i < pointers.length; ++i) {
                    pointers[i] = mm.newPointer(primitive[i]);
                }
            }
        }
    }
}

