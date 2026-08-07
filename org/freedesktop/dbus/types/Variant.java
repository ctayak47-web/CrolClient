
package org.freedesktop.dbus.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.utils.DBusObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Variant<T> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final T value;
    private final Type type;
    private final String sig;

    public Variant(T _value) throws IllegalArgumentException {
        DBusObjects.requireNotNull(_value, () -> new IllegalArgumentException("Can't wrap Null in a Variant"));
        this.type = _value.getClass();
        try {
            String[] ss = Marshalling.getDBusType(_value.getClass(), true);
            if (ss.length != 1) {
                throw new IllegalArgumentException("Can't wrap a multi-valued type in a Variant: " + String.valueOf(this.type));
            }
            this.sig = ss[0];
        }
        catch (DBusException _ex) {
            this.logger.debug("Cannot create variant", _ex);
            throw new IllegalArgumentException(String.format("Can't wrap %s in an unqualified Variant (%s).", _value.getClass(), _ex.getMessage()));
        }
        this.value = _value;
    }

    public Variant(T _value, Type _type) throws IllegalArgumentException {
        DBusObjects.requireNotNull(_value, () -> new IllegalArgumentException("Can't wrap Null in a Variant"));
        this.type = _type;
        try {
            String[] ss = Marshalling.getDBusType(_type);
            if (ss.length != 1) {
                throw new IllegalArgumentException("Can't wrap a multi-valued type in a Variant: " + String.valueOf(_type));
            }
            this.sig = ss[0];
        }
        catch (DBusException _ex) {
            this.logger.debug("Cannot create variant", _ex);
            throw new IllegalArgumentException(String.format("Can't wrap %s in an unqualified Variant (%s).", _type, _ex.getMessage()));
        }
        this.value = _value;
    }

    public Variant(T _value, String _sig) throws IllegalArgumentException {
        DBusObjects.requireNotNull(_value, () -> new IllegalArgumentException("Can't wrap Null in a Variant"));
        this.sig = _sig;
        try {
            ArrayList<Type> ts = new ArrayList<Type>();
            Marshalling.getJavaType(_sig, ts, 1);
            if (ts.size() != 1) {
                throw new IllegalArgumentException("Can't wrap multiple or no types in a Variant: " + _sig);
            }
            this.type = (Type)ts.get(0);
        }
        catch (DBusException _ex) {
            this.logger.debug("Cannot create variant", _ex);
            throw new IllegalArgumentException(String.format("Can''t wrap %s in an unqualified Variant (%s).", _sig, _ex.getMessage()));
        }
        this.value = _value;
    }

    public T getValue() {
        return this.value;
    }

    public Type getType() {
        return this.type;
    }

    public String getSig() {
        return this.sig;
    }

    public String toString() {
        return "[" + String.valueOf(this.value) + "]";
    }

    public int hashCode() {
        return Objects.hash(this.value);
    }

    public boolean equals(Object _obj) {
        if (this == _obj) {
            return true;
        }
        if (!(_obj instanceof Variant)) {
            return false;
        }
        Variant other = (Variant)_obj;
        return Objects.equals(this.value, other.value);
    }
}

