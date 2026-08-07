
package jnr.ffi.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

public final class BufferUtil {
    private BufferUtil() {
    }

    public static void putString(ByteBuffer buf, Charset charset, String value) {
        BufferUtil.putCharSequence(buf, charset, (CharSequence)value);
    }

    public static String getString(ByteBuffer buf, Charset charset) {
        return BufferUtil.getCharSequence(buf, charset).toString();
    }

    public static void putCharSequence(ByteBuffer buf, Charset charset, CharSequence value) {
        BufferUtil.putCharSequence(buf, charset.newEncoder(), value);
    }

    public static void putCharSequence(ByteBuffer buf, CharsetEncoder encoder, CharSequence value) {
        encoder.reset().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).encode(CharBuffer.wrap(value), buf, true);
        encoder.flush(buf);
        int nulSize = Math.round(encoder.maxBytesPerChar());
        if (nulSize == 4) {
            buf.putInt(0);
        } else if (nulSize == 2) {
            buf.putShort((short)0);
        } else if (nulSize == 1) {
            buf.put((byte)0);
        }
    }

    public static CharSequence getCharSequence(ByteBuffer buf, Charset charset) {
        ByteBuffer buffer = buf.slice();
        int end = BufferUtil.indexOf(buffer, (byte)0);
        if (end < 0) {
            end = buffer.limit();
        }
        buffer.position(0).limit(end);
        return charset.decode(buffer);
    }

    public static CharSequence getCharSequence(ByteBuffer buf, CharsetDecoder decoder) {
        ByteBuffer buffer = buf.slice();
        int end = BufferUtil.indexOf(buffer, (byte)0);
        if (end < 0) {
            end = buffer.limit();
        }
        buffer.position(0).limit(end);
        try {
            return decoder.reset().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).decode(buffer);
        }
        catch (CharacterCodingException ex) {
            throw new Error("Illegal character data in native string", ex);
        }
    }

    public static int positionOf(ByteBuffer buf, byte value) {
        if (buf.hasArray()) {
            byte[] array = buf.array();
            int offset = buf.arrayOffset();
            int limit = buf.limit();
            for (int pos = buf.position(); pos < limit; ++pos) {
                if (array[offset + pos] != value) continue;
                return pos;
            }
        } else {
            int limit = buf.limit();
            for (int pos = buf.position(); pos < limit; ++pos) {
                if (buf.get(pos) != value) continue;
                return pos;
            }
        }
        return -1;
    }

    public static int indexOf(ByteBuffer buf, byte value) {
        if (buf.hasArray()) {
            byte[] array = buf.array();
            int begin = buf.arrayOffset() + buf.position();
            int end = buf.arrayOffset() + buf.limit();
            for (int offset = 0; offset < end && offset > -1; ++offset) {
                if (array[begin + offset] != value) continue;
                return offset;
            }
        } else {
            int begin = buf.position();
            for (int offset = 0; offset < buf.limit(); ++offset) {
                if (buf.get(begin + offset) != value) continue;
                return offset;
            }
        }
        return -1;
    }

    public static int indexOf(ByteBuffer buf, int offset, byte value) {
        if (buf.hasArray()) {
            byte[] array = buf.array();
            int begin = buf.arrayOffset() + buf.position() + offset;
            int end = buf.arrayOffset() + buf.limit();
            for (int idx = 0; idx < end && idx > -1; ++idx) {
                if (array[begin + idx] != value) continue;
                return idx;
            }
        } else {
            int begin = buf.position();
            for (int idx = 0; idx < buf.limit(); ++idx) {
                if (buf.get(begin + idx) != value) continue;
                return idx;
            }
        }
        return -1;
    }

    public static ByteBuffer slice(ByteBuffer buffer, int position) {
        ByteBuffer tmp = buffer.duplicate();
        tmp.position(position);
        return tmp.slice();
    }

    public static ByteBuffer slice(ByteBuffer buffer, int position, int size) {
        ByteBuffer tmp = buffer.duplicate();
        tmp.position(position).limit(position + size);
        return tmp.slice();
    }
}

