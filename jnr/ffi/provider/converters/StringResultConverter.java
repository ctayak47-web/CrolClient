
package jnr.ffi.provider.converters;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.Collection;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Encoding;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.MethodResultContext;
import jnr.ffi.provider.converters.StringUtil;

@FromNativeConverter.NoContext
@FromNativeConverter.Cacheable
public class StringResultConverter
implements FromNativeConverter<String, Pointer> {
    private static final FromNativeConverter<String, Pointer> DEFAULT = new StringResultConverter(Charset.defaultCharset());
    private final ThreadLocal<Reference<CharsetDecoder>> localDecoder = new ThreadLocal();
    private final Charset charset;
    private final int terminatorWidth;

    private StringResultConverter(Charset charset) {
        this.charset = charset;
        this.terminatorWidth = StringUtil.terminatorWidth(charset);
    }

    public static FromNativeConverter<String, Pointer> getInstance(Charset cs) {
        return Charset.defaultCharset().equals(cs) ? DEFAULT : new StringResultConverter(cs);
    }

    public static FromNativeConverter<String, Pointer> getInstance(FromNativeContext fromNativeContext) {
        Encoding e;
        Charset charset = Charset.defaultCharset();
        if (fromNativeContext instanceof MethodResultContext && (e = StringResultConverter.getEncoding(Arrays.asList(((MethodResultContext)fromNativeContext).getMethod().getDeclaringClass().getAnnotations()))) != null) {
            charset = Charset.forName(e.value());
        }
        if ((e = StringResultConverter.getEncoding(fromNativeContext.getAnnotations())) != null) {
            charset = Charset.forName(e.value());
        }
        return StringResultConverter.getInstance(charset);
    }

    @Override
    public String fromNative(Pointer pointer, FromNativeContext context) {
        if (pointer == null) {
            return null;
        }
        int idx = 0;
        block2: while (true) {
            idx += pointer.indexOf(idx, (byte)0);
            for (int tcount = 1; tcount < this.terminatorWidth; ++tcount) {
                if (pointer.getByte(idx + tcount) == 0) continue;
                idx += tcount;
                continue block2;
            }
            break;
        }
        byte[] bytes = new byte[idx];
        pointer.get(0L, bytes, 0, bytes.length);
        try {
            return StringUtil.getDecoder(this.charset, this.localDecoder).reset().decode(ByteBuffer.wrap(bytes)).toString();
        }
        catch (CharacterCodingException cce) {
            throw new RuntimeException(cce);
        }
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    private static Encoding getEncoding(Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            if (!(a instanceof Encoding)) continue;
            return (Encoding)a;
        }
        return null;
    }
}

