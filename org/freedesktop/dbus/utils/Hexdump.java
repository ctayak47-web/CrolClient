
package org.freedesktop.dbus.utils;

import java.io.PrintStream;

public final class Hexdump {
    private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private Hexdump() {
    }

    public static String toHex(byte[] _buf) {
        return Hexdump.toHex(_buf, true);
    }

    public static String toHex(byte[] _buf, boolean _spaces) {
        return Hexdump.toHex(_buf, 0, _buf.length, _spaces);
    }

    public static String toHex(byte[] _buf, int _ofs, int _len, boolean _spaces) {
        StringBuilder sb = new StringBuilder();
        int j = _ofs + _len;
        for (int i = _ofs; i < j; ++i) {
            if (i < _buf.length) {
                sb.append(HEX_CHARS[(_buf[i] & 0xF0) >> 4]);
                sb.append(HEX_CHARS[_buf[i] & 0xF]);
                if (!_spaces) continue;
                sb.append(' ');
                continue;
            }
            if (!_spaces) continue;
            sb.append(' ');
            sb.append(' ');
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String toAscii(byte[] _buf) {
        return Hexdump.toAscii(_buf, 0, _buf.length);
    }

    public static String toAscii(byte[] _buf, int _ofs, int _len) {
        StringBuilder sb = new StringBuilder();
        int j = _ofs + _len;
        for (int i = _ofs; i < j; ++i) {
            if (i < _buf.length) {
                if (20 <= _buf[i] && 126 >= _buf[i]) {
                    sb.append((char)_buf[i]);
                    continue;
                }
                sb.append('.');
                continue;
            }
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String format(byte[] _buf) {
        return Hexdump.format(_buf, 80);
    }

    public static String format(byte[] _buf, int _width) {
        int bs = (_width - 8) / 4;
        int i = 0;
        StringBuilder sb = new StringBuilder();
        do {
            for (int j = 0; j < 6; ++j) {
                sb.append(HEX_CHARS[(i << j * 4 & 0xF00000) >> 20]);
            }
            sb.append('\t');
            sb.append(Hexdump.toHex(_buf, i, bs, true));
            sb.append(' ');
            sb.append(Hexdump.toAscii(_buf, i, bs));
            sb.append('\n');
        } while ((i += bs) < _buf.length);
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static void print(byte[] _buf) {
        Hexdump.print(_buf, System.err);
    }

    public static void print(byte[] _buf, int _width) {
        Hexdump.print(_buf, _width, System.err);
    }

    public static void print(byte[] _buf, int _width, PrintStream _out) {
        _out.print(Hexdump.format(_buf, _width));
    }

    public static void print(byte[] _buf, PrintStream _out) {
        _out.print(Hexdump.format(_buf));
    }

    public static String toByteArray(byte[] _buf) {
        return Hexdump.toByteArray(_buf, 0, _buf.length);
    }

    public static String toByteArray(byte[] _buf, int _ofs, int _len) {
        StringBuilder sb = new StringBuilder();
        for (int i = _ofs; i < _len && i < _buf.length; ++i) {
            sb.append('0');
            sb.append('x');
            sb.append(HEX_CHARS[(_buf[i] & 0xF0) >> 4]);
            sb.append(HEX_CHARS[_buf[i] & 0xF]);
            if (i + 1 >= _len || i + 1 >= _buf.length) continue;
            sb.append(',');
        }
        return sb.toString();
    }
}

