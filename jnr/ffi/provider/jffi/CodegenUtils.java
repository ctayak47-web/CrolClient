
package jnr.ffi.provider.jffi;

import java.util.Arrays;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

public class CodegenUtils {
    public static String c(String p) {
        return p.replace('/', '.');
    }

    public static String p(Class n) {
        return n.getName().replace('.', '/');
    }

    public static String p(String n) {
        return n.replace('.', '/');
    }

    public static String ci(Class n) {
        if (n.isArray()) {
            if ((n = n.getComponentType()).isPrimitive()) {
                if (n == Byte.TYPE) {
                    return "[B";
                }
                if (n == Boolean.TYPE) {
                    return "[Z";
                }
                if (n == Short.TYPE) {
                    return "[S";
                }
                if (n == Character.TYPE) {
                    return "[C";
                }
                if (n == Integer.TYPE) {
                    return "[I";
                }
                if (n == Float.TYPE) {
                    return "[F";
                }
                if (n == Double.TYPE) {
                    return "[D";
                }
                if (n == Long.TYPE) {
                    return "[J";
                }
                throw new RuntimeException("Unrecognized type in compiler: " + n.getName());
            }
            return "[" + CodegenUtils.ci(n);
        }
        if (n.isPrimitive()) {
            if (n == Byte.TYPE) {
                return "B";
            }
            if (n == Boolean.TYPE) {
                return "Z";
            }
            if (n == Short.TYPE) {
                return "S";
            }
            if (n == Character.TYPE) {
                return "C";
            }
            if (n == Integer.TYPE) {
                return "I";
            }
            if (n == Float.TYPE) {
                return "F";
            }
            if (n == Double.TYPE) {
                return "D";
            }
            if (n == Long.TYPE) {
                return "J";
            }
            if (n == Void.TYPE) {
                return "V";
            }
            throw new RuntimeException("Unrecognized type in compiler: " + n.getName());
        }
        return "L" + CodegenUtils.p(n) + ";";
    }

    public static String human(Class n) {
        return n.getCanonicalName();
    }

    public static String sig(Class retval, Class ... params) {
        return CodegenUtils.sigParams(params) + CodegenUtils.ci(retval);
    }

    public static String sig(Class retval, String descriptor2, Class ... params) {
        return CodegenUtils.sigParams(descriptor2, params) + CodegenUtils.ci(retval);
    }

    public static String sigParams(Class ... params) {
        StringBuilder signature = new StringBuilder("(");
        for (int i = 0; i < params.length; ++i) {
            signature.append(CodegenUtils.ci(params[i]));
        }
        signature.append(")");
        return signature.toString();
    }

    public static String sigParams(String descriptor2, Class ... params) {
        StringBuilder signature = new StringBuilder("(");
        signature.append(descriptor2);
        for (int i = 0; i < params.length; ++i) {
            signature.append(CodegenUtils.ci(params[i]));
        }
        signature.append(")");
        return signature.toString();
    }

    public static String pretty(Class retval, Class ... params) {
        return CodegenUtils.prettyParams(params) + CodegenUtils.human(retval);
    }

    public static String prettyParams(Class ... params) {
        StringBuilder signature = new StringBuilder("(");
        for (int i = 0; i < params.length; ++i) {
            signature.append(CodegenUtils.human(params[i]));
            if (i >= params.length - 1) continue;
            signature.append(',');
        }
        signature.append(")");
        return signature.toString();
    }

    public static Class[] params(Class ... classes) {
        return classes;
    }

    public static Class[] params(Class cls, int times) {
        Object[] classes = new Class[times];
        Arrays.fill(classes, cls);
        return classes;
    }

    public static Class[] params(Class cls1, Class clsFill, int times) {
        Object[] classes = new Class[times + 1];
        Arrays.fill(classes, clsFill);
        classes[0] = cls1;
        return classes;
    }

    public static String getAnnotatedBindingClassName(String javaMethodName, String typeName, boolean isStatic, int required, int optional, boolean multi, boolean framed) {
        String marker;
        String string = marker = framed ? "$RUBYFRAMEDINVOKER$" : "$RUBYINVOKER$";
        String commonClassSuffix = multi ? (isStatic ? "$s" : "$i") + "_method_multi" + marker + javaMethodName : (isStatic ? "$s" : "$i") + "_method_" + required + "_" + optional + marker + javaMethodName;
        return typeName + commonClassSuffix;
    }

    public static void visitAnnotationFields(AnnotationVisitor visitor, Map<String, Object> fields) {
        for (Map.Entry<String, Object> fieldEntry : fields.entrySet()) {
            Object value = fieldEntry.getValue();
            if (value.getClass().isArray()) {
                Object[] values2 = (Object[])value;
                AnnotationVisitor arrayV = visitor.visitArray(fieldEntry.getKey());
                for (int i = 0; i < values2.length; ++i) {
                    arrayV.visit(null, values2[i]);
                }
                arrayV.visitEnd();
                continue;
            }
            if (value.getClass().isEnum()) {
                visitor.visitEnum(fieldEntry.getKey(), CodegenUtils.ci(value.getClass()), value.toString());
                continue;
            }
            if (value instanceof Class) {
                visitor.visit(fieldEntry.getKey(), (Object)Type.getType((Class)((Class)value)));
                continue;
            }
            visitor.visit(fieldEntry.getKey(), value);
        }
    }
}

