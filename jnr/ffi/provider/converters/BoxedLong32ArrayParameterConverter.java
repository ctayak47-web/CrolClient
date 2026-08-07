
package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class BoxedLong32ArrayParameterConverter
implements ToNativeConverter<Long[], int[]> {
    private static final ToNativeConverter<Long[], int[]> IN = new BoxedLong32ArrayParameterConverter(2);
    private static final ToNativeConverter<Long[], int[]> OUT = new Out(1);
    private static final ToNativeConverter<Long[], int[]> INOUT = new Out(3);
    private final int parameterFlags;

    public static ToNativeConverter<Long[], int[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? (ParameterFlags.isIn(parameterFlags) ? INOUT : OUT) : IN;
    }

    public BoxedLong32ArrayParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    @Override
    public int[] toNative(Long[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        int[] primitive = new int[array.length];
        if (ParameterFlags.isIn(this.parameterFlags)) {
            for (int i = 0; i < array.length; ++i) {
                primitive[i] = array[i] != null ? array[i].intValue() : 0;
            }
        }
        return primitive;
    }

    @Override
    public Class<int[]> nativeType() {
        return int[].class;
    }

    public static final class Out
    extends BoxedLong32ArrayParameterConverter
    implements ToNativeConverter.PostInvocation<Long[], int[]> {
        Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(Long[] array, int[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; ++i) {
                    array[i] = primitive[i];
                }
            }
        }
    }
}

