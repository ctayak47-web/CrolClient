
package jnr.ffi.provider.converters;

import jnr.ffi.annotations.LongLong;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class BoxedLong64ArrayParameterConverter
implements ToNativeConverter<Long[], long[]> {
    private static final ToNativeConverter<Long[], long[]> IN = new BoxedLong64ArrayParameterConverter(2);
    private static final ToNativeConverter<Long[], long[]> OUT = new Out(1);
    private static final ToNativeConverter<Long[], long[]> INOUT = new Out(3);
    private final int parameterFlags;

    public static ToNativeConverter<Long[], long[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? (ParameterFlags.isIn(parameterFlags) ? INOUT : OUT) : IN;
    }

    public BoxedLong64ArrayParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    @Override
    public long[] toNative(Long[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        long[] primitive = new long[array.length];
        if (ParameterFlags.isIn(this.parameterFlags)) {
            for (int i = 0; i < array.length; ++i) {
                primitive[i] = array[i] != null ? array[i] : 0L;
            }
        }
        return primitive;
    }

    @Override
    @LongLong
    public Class<long[]> nativeType() {
        return long[].class;
    }

    public static final class Out
    extends BoxedLong64ArrayParameterConverter
    implements ToNativeConverter.PostInvocation<Long[], long[]> {
        Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(Long[] array, long[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; ++i) {
                    array[i] = primitive[i];
                }
            }
        }
    }
}

