
package org.freedesktop.dbus.connections.config;

import java.util.OptionalLong;
import org.freedesktop.dbus.connections.SASL;

public class SaslConfig {
    private SASL.SaslMode mode = SASL.SaslMode.CLIENT;
    private int authMode = 0;
    private String guid;
    private OptionalLong saslUid = OptionalLong.empty();
    private boolean strictCookiePermissions;
    private boolean fileDescriptorSupport;

    SaslConfig() {
    }

    public SASL.SaslMode getMode() {
        return this.mode;
    }

    public void setMode(SASL.SaslMode _mode) {
        this.mode = _mode;
    }

    public int getAuthMode() {
        return this.authMode;
    }

    public void setAuthMode(int _types) {
        this.authMode = _types;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String _guid) {
        this.guid = _guid;
    }

    public OptionalLong getSaslUid() {
        return this.saslUid;
    }

    public void setSaslUid(OptionalLong _saslUid) {
        this.saslUid = _saslUid;
    }

    public boolean isStrictCookiePermissions() {
        return this.strictCookiePermissions;
    }

    public void setStrictCookiePermissions(boolean _strictCookiePermissions) {
        this.strictCookiePermissions = _strictCookiePermissions;
    }

    public boolean isFileDescriptorSupport() {
        return this.fileDescriptorSupport;
    }

    public void setFileDescriptorSupport(boolean _fileDescriptorSupport) {
        this.fileDescriptorSupport = _fileDescriptorSupport;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [mode=" + String.valueOf((Object)this.mode) + ", authMode=" + this.authMode + ", guid=" + this.guid + ", saslUid=" + String.valueOf(this.saslUid) + ", strictCookiePermissions=" + this.strictCookiePermissions + ", fileDescriptorSupport=" + this.fileDescriptorSupport + "]";
    }
}

