
package org.freedesktop.dbus.interfaces;

import java.util.Map;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.errors.MatchRuleInvalid;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

@DBusInterfaceName(value="org.freedesktop.DBus")
public interface DBus
extends DBusInterface {
    public static final int DBUS_NAME_FLAG_ALLOW_REPLACEMENT = 1;
    public static final int DBUS_NAME_FLAG_REPLACE_EXISTING = 2;
    public static final int DBUS_NAME_FLAG_DO_NOT_QUEUE = 4;
    public static final int DBUS_REQUEST_NAME_REPLY_PRIMARY_OWNER = 1;
    public static final int DBUS_REQUEST_NAME_REPLY_IN_QUEUE = 2;
    public static final int DBUS_REQUEST_NAME_REPLY_EXISTS = 3;
    public static final int DBUS_REQUEST_NAME_REPLY_ALREADY_OWNER = 4;
    public static final int DBUS_RELEASE_NAME_REPLY_RELEASED = 1;
    public static final int DBUS_RELEASE_NAME_REPLY_NON_EXISTANT = 2;
    public static final int DBUS_RELEASE_NAME_REPLY_NOT_OWNER = 3;
    public static final int DBUS_START_REPLY_SUCCESS = 1;
    public static final int DBUS_START_REPLY_ALREADY_RUNNING = 2;

    public String Hello();

    public UInt32 RequestName(String var1, UInt32 var2);

    public UInt32 ReleaseName(String var1);

    public String[] ListQueuedOwners(String var1);

    public String[] ListNames();

    public String[] ListActivatableNames();

    public boolean NameHasOwner(String var1);

    public UInt32 StartServiceByName(String var1, UInt32 var2);

    public void UpdateActivationEnvironment(Map<String, String>[] var1);

    public String GetNameOwner(String var1);

    public UInt32 GetConnectionUnixUser(String var1);

    public UInt32 GetConnectionUnixProcessID(String var1);

    public Map<String, Variant<?>> GetConnectionCredentials(String var1);

    public Byte[] GetAdtAuditSessionData(String var1);

    public Byte[] GetConnectionSELinuxSecurityContext(String var1);

    public void AddMatch(String var1) throws MatchRuleInvalid;

    public void RemoveMatch(String var1) throws MatchRuleInvalid;

    public String GetId();

    public static class NameAcquired
    extends DBusSignal {
        public final String name;

        public NameAcquired(String _path, String _name) throws DBusException {
            super(_path, _name);
            this.name = _name;
        }
    }

    public static class NameLost
    extends DBusSignal {
        public final String name;

        public NameLost(String _path, String _name) throws DBusException {
            super(_path, _name);
            this.name = _name;
        }
    }

    public static class NameOwnerChanged
    extends DBusSignal {
        public final String name;
        public final String oldOwner;
        public final String newOwner;

        public NameOwnerChanged(String _path, String _name, String _oldOwner, String _newOwner) throws DBusException {
            super(_path, _name, _oldOwner, _newOwner);
            this.name = _name;
            this.oldOwner = _oldOwner;
            this.newOwner = _newOwner;
        }
    }
}

