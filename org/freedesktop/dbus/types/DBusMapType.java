
package org.freedesktop.dbus.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class DBusMapType
implements ParameterizedType {
    private final Type k;
    private final Type v;

    public DBusMapType(Type _k, Type _v) {
        this.k = _k;
        this.v = _v;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{this.k, this.v};
    }

    @Override
    public Type getRawType() {
        return Map.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}

