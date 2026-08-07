
package jnr.ffi.provider.converters;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Collection;
import jnr.ffi.annotations.Encoding;
import jnr.ffi.mapper.MethodParameterContext;
import jnr.ffi.mapper.ToNativeContext;

final class StringUtil {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Charset USASCII = Charset.forName("US-ASCII");
    private static final Charset ISO8859_1 = Charset.forName("ISO-8859-1");
    private static final Charset UTF16 = Charset.forName("UTF-16");
    private static final Charset UTF16LE = Charset.forName("UTF-16LE");
    private static final Charset UTF16BE = Charset.forName("UTF-16BE");

    private StringUtil() {
    }

    static CharsetEncoder getEncoder(Charset charset, ThreadLocal<Reference<CharsetEncoder>> localEncoder) {
        CharsetEncoder encoder;
        Reference<CharsetEncoder> ref = localEncoder.get();
        return ref != null && (encoder = ref.get()) != null && encoder.charset() == charset ? encoder : StringUtil.initEncoder(charset, localEncoder);
    }

    static CharsetDecoder getDecoder(Charset charset, ThreadLocal<Reference<CharsetDecoder>> localDecoder) {
        CharsetDecoder decoder;
        Reference<CharsetDecoder> ref = localDecoder.get();
        return ref != null && (decoder = ref.get()) != null && decoder.charset() == charset ? decoder : StringUtil.initDecoder(charset, localDecoder);
    }

    private static CharsetEncoder initEncoder(Charset charset, ThreadLocal<Reference<CharsetEncoder>> localEncoder) {
        CharsetEncoder encoder = charset.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        localEncoder.set(new SoftReference<CharsetEncoder>(encoder));
        return encoder;
    }

    private static CharsetDecoder initDecoder(Charset charset, ThreadLocal<Reference<CharsetDecoder>> localDecoder) {
        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        localDecoder.set(new SoftReference<CharsetDecoder>(decoder));
        return decoder;
    }

    static Charset getCharset(ToNativeContext toNativeContext) {
        Charset cs;
        Charset charset = Charset.defaultCharset();
        if (toNativeContext instanceof MethodParameterContext) {
            cs = StringUtil.getEncodingCharset(Arrays.asList(((MethodParameterContext)toNativeContext).getMethod().getDeclaringClass().getAnnotations()));
            if (cs != null) {
                charset = cs;
            }
            if ((cs = StringUtil.getEncodingCharset(Arrays.asList(((MethodParameterContext)toNativeContext).getMethod().getAnnotations()))) != null) {
                charset = cs;
            }
        }
        if ((cs = StringUtil.getEncodingCharset(toNativeContext.getAnnotations())) != null) {
            charset = cs;
        }
        return charset;
    }

    private static Charset getEncodingCharset(Collection<Annotation> annotations) {
        for (Annotation a : annotations) {
            if (!(a instanceof Encoding)) continue;
            return Charset.forName(((Encoding)a).value());
        }
        return null;
    }

    static void throwException(CoderResult result) {
        try {
            result.throwException();
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (CharacterCodingException cce) {
            throw new RuntimeException(cce);
        }
    }

    static int terminatorWidth(Charset charset) {
        if (charset.equals(UTF8) || charset.equals(USASCII) || charset.equals(ISO8859_1)) {
            return 1;
        }
        if (charset.equals(UTF16) || charset.equals(UTF16LE) || charset.equals(UTF16BE)) {
            return 2;
        }
        return 4;
    }

    static int stringLength(ByteBuffer in, int terminatorWidth) {
        if (in.hasArray()) {
            byte[] array = in.array();
            int end = in.arrayOffset() + in.limit();
            int tcount = 0;
            int idx = in.arrayOffset() + in.position();
            while (idx < end) {
                tcount = array[idx++] == 0 ? ++tcount : 0;
                if (tcount != terminatorWidth) continue;
                return idx - terminatorWidth;
            }
        } else {
            int begin = in.position();
            int end = in.limit();
            int tcount = 0;
            int idx = begin;
            while (idx < end) {
                tcount = in.get(idx++) == 0 ? ++tcount : 0;
                if (tcount != terminatorWidth) continue;
                return idx - terminatorWidth;
            }
        }
        return -1;
    }
}

