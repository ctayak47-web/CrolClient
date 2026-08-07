
package org.freedesktop.dbus.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.regex.Pattern;
import org.freedesktop.dbus.annotations.DBusBoundProperty;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;

public final class DBusNamingUtil {
    private static final Pattern DOLLAR_PATTERN = Pattern.compile("[$]");

    private DBusNamingUtil() {
    }

    public static String getInterfaceName(Class<?> _clazz) {
        Objects.requireNonNull(_clazz, "Class must not be null");
        if (_clazz.isAnnotationPresent(DBusInterfaceName.class)) {
            return _clazz.getAnnotation(DBusInterfaceName.class).value();
        }
        return DOLLAR_PATTERN.matcher(_clazz.getName()).replaceAll(".");
    }

    public static String getMethodName(Method _method) {
        Objects.requireNonNull(_method, "method must not be null");
        if (_method.isAnnotationPresent(DBusMemberName.class)) {
            return _method.getAnnotation(DBusMemberName.class).value();
        }
        return _method.getName();
    }

    public static String getPropertyName(Method _method) {
        String defName;
        Objects.requireNonNull(_method, "method must not be null");
        if (_method.isAnnotationPresent(DBusBoundProperty.class) && !"".equals(defName = _method.getAnnotation(DBusBoundProperty.class).name())) {
            return defName;
        }
        String name = _method.getName();
        String lowerCaseName = name.toLowerCase();
        if (lowerCaseName.startsWith("get") && !"get".equals(lowerCaseName) || lowerCaseName.startsWith("set") && !"set".equals(lowerCaseName)) {
            name = name.substring(3);
        } else if (lowerCaseName.startsWith("is") && !"is".equals(lowerCaseName)) {
            name = name.substring(2);
        }
        return name;
    }

    public static String getSignalName(Class<?> _clazz) {
        Objects.requireNonNull(_clazz, "Class must not be null");
        if (_clazz.isAnnotationPresent(DBusMemberName.class)) {
            return _clazz.getAnnotation(DBusMemberName.class).value();
        }
        return _clazz.getSimpleName();
    }

    public static String getAnnotationName(Class<? extends Annotation> _clazz) {
        Objects.requireNonNull(_clazz, "Class must not be null");
        if (_clazz.isAnnotationPresent(DBusInterfaceName.class)) {
            return _clazz.getAnnotation(DBusInterfaceName.class).value();
        }
        return DOLLAR_PATTERN.matcher(_clazz.getName()).replaceAll(".");
    }
}

