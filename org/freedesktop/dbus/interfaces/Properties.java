
package org.freedesktop.dbus.interfaces;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

@DBusInterfaceName(value="org.freedesktop.DBus.Properties")
public interface Properties
extends DBusInterface {
    public <A> A Get(String var1, String var2);

    public <A> void Set(String var1, String var2, A var3);

    public Map<String, Variant<?>> GetAll(String var1);

    public static class PropertiesChanged
    extends DBusSignal {
        private final Map<String, Variant<?>> propertiesChanged;
        private final List<String> propertiesRemoved;
        private final String interfaceName;

        public PropertiesChanged(String _path, String _interfaceName, Map<String, Variant<?>> _propertiesChanged, List<String> _propertiesRemoved) throws DBusException {
            super(_path, _interfaceName, _propertiesChanged, _propertiesRemoved);
            this.propertiesChanged = _propertiesChanged;
            this.propertiesRemoved = _propertiesRemoved;
            this.interfaceName = _interfaceName;
        }

        public String getInterfaceName() {
            return this.interfaceName;
        }

        public Map<String, Variant<?>> getPropertiesChanged() {
            return this.propertiesChanged;
        }

        public List<String> getPropertiesRemoved() {
            return this.propertiesRemoved;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "[propertiesChanged=" + String.valueOf(this.propertiesChanged) + ", propertiesRemoved=" + String.valueOf(this.propertiesRemoved) + ", interfaceName='" + this.interfaceName + "']";
        }
    }
}

