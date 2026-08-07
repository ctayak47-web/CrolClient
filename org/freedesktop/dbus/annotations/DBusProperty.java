
package org.freedesktop.dbus.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.freedesktop.dbus.annotations.DBusProperties;
import org.freedesktop.dbus.types.Variant;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@Repeatable(value=DBusProperties.class)
public @interface DBusProperty {
    public String name();

    public Class<?> type() default Variant.class;

    public Access access() default Access.READ_WRITE;

    public static enum Access {
        READ("read"),
        READ_WRITE("readwrite"),
        WRITE("write");

        private final String accessName;

        private Access(String _accessName) {
            this.accessName = _accessName;
        }

        public String getAccessName() {
            return this.accessName;
        }
    }
}

