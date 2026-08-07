
package org.freedesktop.dbus.connections.config;

import java.util.OptionalLong;
import org.freedesktop.dbus.connections.config.SaslConfig;
import org.freedesktop.dbus.connections.config.TransportConfigBuilder;
import org.freedesktop.dbus.connections.transports.TransportBuilder;

public final class SaslConfigBuilder<R> {
    private SaslConfig saslConfig = new SaslConfig();
    private final TransportConfigBuilder<?, R> transportBuilder;

    SaslConfigBuilder(TransportConfigBuilder<?, R> _transportBuilder) {
        this.transportBuilder = _transportBuilder;
    }

    public TransportConfigBuilder<?, R> back() {
        return this.transportBuilder;
    }

    public SaslConfigBuilder<R> withAuthMode(TransportBuilder.SaslAuthMode _types) {
        if (_types != null) {
            this.saslConfig.setAuthMode(_types.getAuthMode());
        }
        return this;
    }

    public SaslConfigBuilder<R> withSaslUid(Long _saslUid) {
        this.saslConfig.setSaslUid(OptionalLong.of(_saslUid));
        return this;
    }

    public SaslConfigBuilder<R> withStrictCookiePermissions(boolean _strictCookiePermissions) {
        this.saslConfig.setStrictCookiePermissions(_strictCookiePermissions);
        return this;
    }

    public SaslConfig build() {
        return this.saslConfig;
    }

    SaslConfigBuilder<R> withConfig(SaslConfig _cfg) {
        if (_cfg != null) {
            this.saslConfig = _cfg;
        }
        return this;
    }
}

