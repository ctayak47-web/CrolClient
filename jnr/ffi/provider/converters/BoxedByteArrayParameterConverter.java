
package jnr.ffi.provider.converters;

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class BoxedByteArrayParameterConverter
implements ToNativeConverter<Byte[], byte[]> {
    private static final ToNativeConverter<Byte[], byte[]> IN = new BoxedByteArrayParameterConverter(2);
    private static final ToNativeConverter<Byte[], byte[]> OUT = new Out(1);
    private static final ToNativeConverter<Byte[], byte[]> INOUT = new Out(3);
    private final int parameterFlags;

    public static ToNativeConverter<Byte[], byte[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? (ParameterFlags.isIn(parameterFlags) ? INOUT : OUT) : IN;
    }

    BoxedByteArrayParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    @Override
    public byte[] toNative(Byte[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        byte[] primitive = new byte[array.length];
        if (ParameterFlags.isIn(this.parameterFlags)) {
            for (int i = 0; i < array.length; ++i) {
                primitive[i] = array[i] != null ? array[i] : (byte)0;
            }
        }
        return primitive;
    }

    @Override
    public Class<byte[]> nativeType() {
        return byte[].class;
    }

    public static final class Out
    extends BoxedByteArrayParameterConverter
    implements ToNativeConverter.PostInvocation<Byte[], byte[]> {
        Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(Byte[] array, byte[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; ++i) {
                    array[i] = primitive[i];
                }
            }
        }
    }
}

