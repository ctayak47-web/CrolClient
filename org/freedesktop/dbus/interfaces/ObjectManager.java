
package org.freedesktop.dbus.interfaces;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

@DBusInterfaceName(value="org.freedesktop.DBus.ObjectManager")
public interface ObjectManager
extends DBusInterface {
    public Map<DBusPath, Map<String, Map<String, Variant<?>>>> GetManagedObjects();

    public static class InterfacesRemoved
    extends DBusSignal {
        public final DBusPath signalSource;
        public final String objectPath;
        public final List<String> interfaces;

        public InterfacesRemoved(String _objectPath, DBusPath _source, List<String> _interfaces) throws DBusException {
            super(_objectPath, _source, _interfaces);
            this.objectPath = _objectPath;
            this.signalSource = _source;
            this.interfaces = _interfaces;
        }

        public DBusPath getSignalSource() {
            return this.signalSource;
        }

        public String getObjectPath() {
            return this.objectPath;
        }

        public List<String> getInterfaces() {
            return this.interfaces;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "[signalSource=" + String.valueOf(this.signalSource) + ", objectPath='" + this.objectPath + "', interfaces=" + String.valueOf(this.interfaces) + "]";
        }
    }

    public static class InterfacesAdded
    extends DBusSignal {
        public final DBusPath signalSource;
        public final String objectPath;
        public final Map<String, Map<String, Variant<?>>> interfaces;

        public InterfacesAdded(String _objectPath, DBusPath _source, Map<String, Map<String, Variant<?>>> _interfaces) throws DBusException {
            super(_objectPath, _source, _interfaces);
            this.objectPath = _objectPath;
            this.signalSource = _source;
            this.interfaces = _interfaces;
        }

        public DBusPath getSignalSource() {
            return this.signalSource;
        }

        public String getObjectPath() {
            return this.objectPath;
        }

        public Map<String, Map<String, Variant<?>>> getInterfaces() {
            return this.interfaces;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "[signalSource=" + String.valueOf(this.signalSource) + ", objectPath='" + this.objectPath + "', interfaces=" + String.valueOf(this.interfaces) + "]";
        }
    }
}

