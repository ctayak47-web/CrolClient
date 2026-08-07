
package org.freedesktop.dbus.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.freedesktop.dbus.annotations.DBusInterfaceName;

@Retention(value=RetentionPolicy.RUNTIME)
@DBusInterfaceName(value="org.freedesktop.DBus.Description")
public @interface IntrospectionDescription {
    public String value();
}

