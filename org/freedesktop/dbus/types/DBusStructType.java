
package org.freedesktop.dbus.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.freedesktop.dbus.Struct;

public class DBusStructType
implements ParameterizedType {
    private final Type[] contents;

    public DBusStructType(Type ... _contents) {
        this.contents = _contents;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return this.contents;
    }

    @Override
    public Type getRawType() {
        return Struct.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}

