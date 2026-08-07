
package org.freedesktop.dbus.propertyref;

import java.lang.reflect.Method;
import java.util.Objects;
import org.freedesktop.dbus.annotations.DBusBoundProperty;
import org.freedesktop.dbus.annotations.DBusProperty;

public final class PropertyRef {
    private final String name;
    private final Class<?> type;
    private final DBusProperty.Access access;

    public PropertyRef(String _name, Class<?> _type, DBusProperty.Access _access) {
        this.name = _name;
        this.type = _type;
        this.access = _access;
    }

    public PropertyRef(DBusProperty _property) {
        this(_property.name(), _property.type(), _property.access());
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.access, this.name});
    }

    public boolean equals(Object _obj) {
        if (this == _obj) {
            return true;
        }
        if (_obj == null) {
            return false;
        }
        if (this.getClass() != _obj.getClass()) {
            return false;
        }
        PropertyRef other = (PropertyRef)_obj;
        return this.access == other.access && Objects.equals(this.name, other.name);
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getType() {
        return this.type;
    }

    public DBusProperty.Access getAccess() {
        return this.access;
    }

    public static DBusProperty.Access accessForMethod(Method _method) {
        DBusProperty.Access access;
        DBusBoundProperty annotation = _method.getAnnotation(DBusBoundProperty.class);
        DBusProperty.Access access2 = access = _method.getName().toLowerCase().startsWith("set") ? DBusProperty.Access.WRITE : DBusProperty.Access.READ;
        if (annotation.access().equals((Object)DBusProperty.Access.READ) || annotation.access().equals((Object)DBusProperty.Access.WRITE)) {
            access = annotation.access();
        }
        return access;
    }

    public static Class<?> typeForMethod(Method _method) {
        DBusBoundProperty annotation = _method.getAnnotation(DBusBoundProperty.class);
        Class<?> type = annotation.type();
        if (type == null || type.equals(Void.class)) {
            if (PropertyRef.accessForMethod(_method) == DBusProperty.Access.READ) {
                return _method.getReturnType();
            }
            return _method.getParameterTypes()[0];
        }
        return type;
    }

    public static void checkMethod(Method _method) {
        DBusProperty.Access access = PropertyRef.accessForMethod(_method);
        if (access == DBusProperty.Access.READ && (_method.getParameterCount() > 0 || _method.getReturnType().equals(Void.TYPE))) {
            throw new IllegalArgumentException("READ properties must have zero parameters, and not return void.");
        }
        if (!(access != DBusProperty.Access.WRITE || _method.getParameterCount() == 1 && _method.getReturnType().equals(Void.TYPE))) {
            throw new IllegalArgumentException("WRITE properties must have exactly 1 parameter, and return void.");
        }
    }
}

