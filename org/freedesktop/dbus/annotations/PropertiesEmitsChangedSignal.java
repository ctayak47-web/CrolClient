
package org.freedesktop.dbus.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.freedesktop.dbus.annotations.DBusInterfaceName;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
@DBusInterfaceName(value="org.freedesktop.DBus.Property.EmitsChangedSignal")
public @interface PropertiesEmitsChangedSignal {
    public EmitChangeSignal value();

    public static enum EmitChangeSignal {
        TRUE,
        INVALIDATES,
        CONST,
        FALSE;

        public String toString() {
            return this.name().toLowerCase();
        }
    }
}

