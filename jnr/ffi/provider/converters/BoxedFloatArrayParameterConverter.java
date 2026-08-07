
package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class BoxedFloatArrayParameterConverter
implements ToNativeConverter<Float[], float[]> {
    private static final ToNativeConverter<Float[], float[]> IN = new BoxedFloatArrayParameterConverter(2);
    private static final ToNativeConverter<Float[], float[]> OUT = new Out(1);
    private static final ToNativeConverter<Float[], float[]> INOUT = new Out(3);
    private final int parameterFlags;

    public static ToNativeConverter<Float[], float[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? (ParameterFlags.isIn(parameterFlags) ? INOUT : OUT) : IN;
    }

    BoxedFloatArrayParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    @Override
    public float[] toNative(Float[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        float[] primitive = new float[array.length];
        if (ParameterFlags.isIn(this.parameterFlags)) {
            for (int i = 0; i < array.length; ++i) {
                primitive[i] = array[i] != null ? array[i].floatValue() : 0.0f;
            }
        }
        return primitive;
    }

    @Override
    public Class<float[]> nativeType() {
        return float[].class;
    }

    public static final class Out
    extends BoxedFloatArrayParameterConverter
    implements ToNativeConverter.PostInvocation<Float[], float[]> {
        Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(Float[] array, float[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; ++i) {
                    array[i] = Float.valueOf(primitive[i]);
                }
            }
        }
    }
}

