
package jnr.ffi.provider.converters;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;
import java.util.Collection;
import jnr.ffi.annotations.Encoding;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.NulTerminate;
import jnr.ffi.mapper.MethodParameterContext;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.converters.StringUtil;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class CharSequenceParameterConverter
implements ToNativeConverter<CharSequence, ByteBuffer> {
    private static final ToNativeConverter<CharSequence, ByteBuffer> DEFAULT = new CharSequenceParameterConverter(Charset.defaultCharset());
    private final ThreadLocal<Reference<CharsetEncoder>> localEncoder = new ThreadLocal();
    private final Charset charset;

    public static ToNativeConverter<CharSequence, ByteBuffer> getInstance(Charset charset, ToNativeContext toNativeContext) {
        return Charset.defaultCharset().equals(charset) ? DEFAULT : new CharSequenceParameterConverter(charset);
    }

    public static ToNativeConverter<CharSequence, ByteBuffer> getInstance(ToNativeContext toNativeContext) {
        Charset cs;
        Charset charset = Charset.defaultCharset();
        if (toNativeContext instanceof MethodParameterContext) {
            cs = CharSequenceParameterConverter.getEncodingCharset(Arrays.asList(((MethodParameterContext)toNativeContext).getMethod().getDeclaringClass().getAnnotations()));
            if (cs != null) {
                charset = cs;
            }
            if ((cs = CharSequenceParameterConverter.getEncodingCharset(Arrays.asList(((MethodParameterContext)toNativeContext).getMethod().getAnnotations()))) != null) {
                charset = cs;
            }
        }
        if ((cs = CharSequenceParameterConverter.getEncodingCharset(toNativeContext.getAnnotations())) != null) {
            charset = cs;
        }
        return CharSequenceParameterConverter.getInstance(charset, toNativeContext);
    }

    private static Charset getEncodingCharset(Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            if (!(a instanceof Encoding)) continue;
            return Charset.forName(((Encoding)a).value());
        }
        return null;
    }

    private CharSequenceParameterConverter(Charset charset) {
        this.charset = charset;
    }

    @Override
    public ByteBuffer toNative(CharSequence string, ToNativeContext context) {
        CoderResult result;
        if (string == null) {
            return null;
        }
        CharsetEncoder encoder = StringUtil.getEncoder(this.charset, this.localEncoder);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[(int)((float)string.length() * encoder.averageBytesPerChar()) + 4]);
        CharBuffer charBuffer = CharBuffer.wrap(string);
        encoder.reset();
        while (!(!charBuffer.hasRemaining() || (result = encoder.encode(charBuffer, byteBuffer, true)).isUnderflow() && (result = encoder.flush(byteBuffer)).isUnderflow())) {
            if (result.isOverflow()) {
                byteBuffer = CharSequenceParameterConverter.grow(byteBuffer);
                continue;
            }
            StringUtil.throwException(result);
        }
        if (byteBuffer.remaining() <= 4) {
            byteBuffer = CharSequenceParameterConverter.grow(byteBuffer);
        }
        byteBuffer.position(byteBuffer.position() + 4);
        byteBuffer.flip();
        return byteBuffer;
    }

    private static ByteBuffer grow(ByteBuffer oldBuffer) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[oldBuffer.capacity() * 2]);
        oldBuffer.flip();
        buf.put(oldBuffer);
        return buf;
    }

    @Override
    @In
    @NulTerminate
    public Class<ByteBuffer> nativeType() {
        return ByteBuffer.class;
    }
}

