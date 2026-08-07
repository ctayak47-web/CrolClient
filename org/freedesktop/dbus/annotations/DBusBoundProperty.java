
package org.freedesktop.dbus.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.freedesktop.dbus.annotations.DBusProperty;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface DBusBoundProperty {
    public String name() default "";

    public Class<?> type() default Void.class;

    public DBusProperty.Access access() default DBusProperty.Access.READ_WRITE;
}

