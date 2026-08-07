
package jnr.ffi.provider.jffi;

import com.kenai.jffi.Type;
import jnr.ffi.NativeType;
import jnr.ffi.provider.SigType;
import jnr.ffi.provider.jffi.SkinnyMethodAdapter;
import org.objectweb.asm.Label;

public final class NumberUtil {
    private NumberUtil() {
    }

    static Class getBoxedClass(Class c) {
        if (!c.isPrimitive()) {
            return c;
        }
        if (Void.TYPE == c) {
            return Void.class;
        }
        if (Byte.TYPE == c) {
            return Byte.class;
        }
        if (Character.TYPE == c) {
            return Character.class;
        }
        if (Short.TYPE == c) {
            return Short.class;
        }
        if (Integer.TYPE == c) {
            return Integer.class;
        }
        if (Long.TYPE == c) {
            return Long.class;
        }
        if (Float.TYPE == c) {
            return Float.class;
        }
        if (Double.TYPE == c) {
            return Double.class;
        }
        if (Boolean.TYPE == c) {
            return Boolean.class;
        }
        throw new IllegalArgumentException("unknown primitive class");
    }

    static Class getPrimitiveClass(Class c) {
        if (Void.class == c) {
            return Void.TYPE;
        }
        if (Boolean.class == c) {
            return Boolean.TYPE;
        }
        if (Byte.class == c) {
            return Byte.TYPE;
        }
        if (Character.class == c) {
            return Character.TYPE;
        }
        if (Short.class == c) {
            return Short.TYPE;
        }
        if (Integer.class == c) {
            return Integer.TYPE;
        }
        if (Long.class == c) {
            return Long.TYPE;
        }
        if (Float.class == c) {
            return Float.TYPE;
        }
        if (Double.class == c) {
            return Double.TYPE;
        }
        if (c.isPrimitive()) {
            return c;
        }
        throw new IllegalArgumentException("unsupported number class");
    }

    public static boolean isPrimitiveInt(Class c) {
        return Byte.TYPE == c || Character.TYPE == c || Short.TYPE == c || Integer.TYPE == c || Boolean.TYPE == c;
    }

    public static void widen(SkinnyMethodAdapter mv, Class from, Class to) {
        if (Long.TYPE == to && Long.TYPE != from && NumberUtil.isPrimitiveInt(from)) {
            mv.i2l();
        } else if (Boolean.TYPE == to && Boolean.TYPE != from && NumberUtil.isPrimitiveInt(from)) {
            mv.iconst_1();
            mv.iand();
        }
    }

    public static void widen(SkinnyMethodAdapter mv, Class from, Class to, NativeType nativeType) {
        if (NumberUtil.isPrimitiveInt(from)) {
            if (nativeType == NativeType.UCHAR) {
                mv.pushInt(255);
                mv.iand();
            } else if (nativeType == NativeType.USHORT) {
                mv.pushInt(65535);
                mv.iand();
            }
            if (Long.TYPE == to) {
                mv.i2l();
                switch (nativeType) {
                    case UINT: 
                    case ULONG: 
                    case ADDRESS: {
                        if (NumberUtil.sizeof(nativeType) >= 8) break;
                        mv.ldc(0xFFFFFFFFL);
                        mv.land();
                    }
                }
            }
        }
    }

    public static void narrow(SkinnyMethodAdapter mv, Class from, Class to) {
        if (!from.equals(to)) {
            if (Byte.TYPE == to || Short.TYPE == to || Character.TYPE == to || Integer.TYPE == to) {
                if (Long.TYPE == from) {
                    mv.l2i();
                }
                if (Byte.TYPE == to) {
                    mv.i2b();
                } else if (Short.TYPE == to) {
                    mv.i2s();
                } else if (Character.TYPE == to) {
                    mv.i2c();
                }
            } else if (Boolean.TYPE == to) {
                Label label_false_branch = new Label();
                Label label_end = new Label();
                if (Long.TYPE == from) {
                    mv.lconst_0();
                    mv.lcmp();
                    mv.ifeq(label_false_branch);
                    mv.iconst_1();
                    mv.go_to(label_end);
                    mv.label(label_false_branch);
                    mv.iconst_0();
                    mv.label(label_end);
                } else {
                    mv.ifeq(label_false_branch);
                    mv.iconst_1();
                    mv.go_to(label_end);
                    mv.label(label_false_branch);
                    mv.iconst_0();
                    mv.label(label_end);
                }
            }
        }
    }

    public static void convertPrimitive(SkinnyMethodAdapter mv, Class from, Class to) {
        NumberUtil.narrow(mv, from, to);
        NumberUtil.widen(mv, from, to);
    }

    public static void convertPrimitive(SkinnyMethodAdapter mv, Class from, Class to, NativeType nativeType) {
        if (Boolean.TYPE == to) {
            NumberUtil.narrow(mv, from, to);
            return;
        }
        switch (nativeType) {
            case SCHAR: {
                NumberUtil.narrow(mv, from, Byte.TYPE);
                NumberUtil.widen(mv, Byte.TYPE, to);
                break;
            }
            case SSHORT: {
                NumberUtil.narrow(mv, from, Short.TYPE);
                NumberUtil.widen(mv, Short.TYPE, to);
                break;
            }
            case SINT: {
                NumberUtil.narrow(mv, from, Integer.TYPE);
                NumberUtil.widen(mv, Integer.TYPE, to);
                break;
            }
            case UCHAR: {
                NumberUtil.narrow(mv, from, Integer.TYPE);
                mv.pushInt(255);
                mv.iand();
                NumberUtil.widen(mv, Integer.TYPE, to);
                break;
            }
            case USHORT: {
                NumberUtil.narrow(mv, from, Integer.TYPE);
                mv.pushInt(65535);
                mv.iand();
                NumberUtil.widen(mv, Integer.TYPE, to);
                break;
            }
            case UINT: 
            case ULONG: 
            case ADDRESS: {
                if (NumberUtil.sizeof(nativeType) <= 4) {
                    NumberUtil.narrow(mv, from, Integer.TYPE);
                    if (Long.TYPE != to) break;
                    mv.i2l();
                    mv.ldc(0xFFFFFFFFL);
                    mv.land();
                    break;
                }
                NumberUtil.widen(mv, from, to);
                break;
            }
            case FLOAT: 
            case DOUBLE: {
                break;
            }
            default: {
                NumberUtil.narrow(mv, from, to);
                NumberUtil.widen(mv, from, to);
            }
        }
    }

    static int sizeof(SigType type) {
        return NumberUtil.sizeof(type.getNativeType());
    }

    static int sizeof(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR: {
                return Type.SCHAR.size();
            }
            case UCHAR: {
                return Type.UCHAR.size();
            }
            case SSHORT: {
                return Type.SSHORT.size();
            }
            case USHORT: {
                return Type.USHORT.size();
            }
            case SINT: {
                return Type.SINT.size();
            }
            case UINT: {
                return Type.UINT.size();
            }
            case SLONG: {
                return Type.SLONG.size();
            }
            case ULONG: {
                return Type.ULONG.size();
            }
            case SLONGLONG: {
                return Type.SLONG_LONG.size();
            }
            case ULONGLONG: {
                return Type.ULONG_LONG.size();
            }
            case FLOAT: {
                return Type.FLOAT.size();
            }
            case DOUBLE: {
                return Type.DOUBLE.size();
            }
            case ADDRESS: {
                return Type.POINTER.size();
            }
            case VOID: {
                return 0;
            }
        }
        throw new UnsupportedOperationException("cannot determine size of " + (Object)((Object)nativeType));
    }
}

