
package jnr.posix.util;

import java.lang.reflect.Field;

public class FieldAccess {
    public static Field getProtectedField(Class klass, String fieldName) {
        Field field = null;
        try {
            field = klass.getDeclaredField(fieldName);
            field.setAccessible(true);
        }
        catch (Exception exception) {
            
        }
        return field;
    }

    public static Object getProtectedFieldValue(Class klass, String fieldName, Object instance) {
        try {
            Field f = FieldAccess.getProtectedField(klass, fieldName);
            return f.get(instance);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}

