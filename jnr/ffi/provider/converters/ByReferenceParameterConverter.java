
package jnr.ffi.provider.converters;

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.Cacheable
public class ByReferenceParameterConverter
implements ToNativeConverter<ByReference, Pointer> {
    private static final ToNativeConverter<ByReference, Pointer> IN = new ByReferenceParameterConverter(2);
    private static final ToNativeConverter<ByReference, Pointer> OUT = new Out(1);
    private static final ToNativeConverter<ByReference, Pointer> INOUT = new Out(3);
    private final int parameterFlags;

    private ByReferenceParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    public static ToNativeConverter<ByReference, Pointer> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? (ParameterFlags.isIn(parameterFlags) ? INOUT : OUT) : IN;
    }

    @Override
    public Pointer toNative(ByReference value, ToNativeContext context) {
        if (value == null) {
            return null;
        }
        Pointer memory = Memory.allocate(context.getRuntime(), value.nativeSize(context.getRuntime()));
        if (ParameterFlags.isIn(this.parameterFlags)) {
            value.toNative(context.getRuntime(), memory, 0L);
        }
        return memory;
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    public static final class Out
    extends ByReferenceParameterConverter
    implements ToNativeConverter.PostInvocation<ByReference, Pointer> {
        public Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(ByReference byReference, Pointer pointer, ToNativeContext context) {
            if (byReference != null && pointer != null) {
                byReference.fromNative(context.getRuntime(), pointer, 0L);
            }
        }
    }
}

