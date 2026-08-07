
package jnr.ffi.provider.converters;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.util.BufferUtil;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class StringBufferParameterConverter
implements ToNativeConverter<StringBuffer, ByteBuffer>,
ToNativeConverter.PostInvocation<StringBuffer, ByteBuffer> {
    private final Charset charset;
    private final int parameterFlags;

    private StringBufferParameterConverter(Charset charset, int parameterFlags) {
        this.charset = charset;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public Class<ByteBuffer> nativeType() {
        return ByteBuffer.class;
    }

    public static StringBufferParameterConverter getInstance(int parameterFlags, ToNativeContext toNativeContext) {
        return new StringBufferParameterConverter(Charset.defaultCharset(), parameterFlags);
    }

    public static StringBufferParameterConverter getInstance(Charset charset, int parameterFlags, ToNativeContext toNativeContext) {
        return new StringBufferParameterConverter(charset, parameterFlags);
    }

    @Override
    public ByteBuffer toNative(StringBuffer parameter, ToNativeContext context) {
        ByteBuffer buf;
        if (parameter == null) {
            return null;
        }
        ByteBuffer byteBuffer = buf = ParameterFlags.isIn(this.parameterFlags) ? this.charset.encode(CharBuffer.wrap(parameter)) : ByteBuffer.allocate(parameter.capacity() + 1);
        if (ParameterFlags.isOut(this.parameterFlags) && buf.capacity() < parameter.capacity() + 1 || !buf.hasArray()) {
            byte[] array = new byte[parameter.capacity() + 1];
            buf.get(array, 0, buf.remaining());
            return ByteBuffer.wrap(array);
        }
        return buf;
    }

    @Override
    public void postInvoke(StringBuffer stringBuffer, ByteBuffer buf, ToNativeContext context) {
        if (ParameterFlags.isOut(this.parameterFlags) && stringBuffer != null && buf != null) {
            buf.limit(buf.capacity());
            stringBuffer.delete(0, stringBuffer.length()).append(BufferUtil.getCharSequence(buf, this.charset));
        }
    }
}

