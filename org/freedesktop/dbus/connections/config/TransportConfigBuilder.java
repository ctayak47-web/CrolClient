
package org.freedesktop.dbus.connections.config;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.config.SaslConfig;
import org.freedesktop.dbus.connections.config.SaslConfigBuilder;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.connections.transports.AbstractTransport;

public class TransportConfigBuilder<X extends TransportConfigBuilder<?, R>, R> {
    private final Supplier<R> connectionBuilder;
    private TransportConfig config = new TransportConfig();
    private final SaslConfigBuilder<R> saslConfigBuilder;

    public TransportConfigBuilder(Supplier<R> _sup) {
        this.connectionBuilder = _sup;
        this.saslConfigBuilder = new SaslConfigBuilder(this);
    }

    X self() {
        return (X)this;
    }

    public X withConfig(TransportConfig _config) {
        this.config = Objects.requireNonNull(_config, "TransportConfig required");
        this.saslConfigBuilder.withConfig(_config.getSaslConfig());
        return this.self();
    }

    public X withBusAddress(BusAddress _address) {
        this.config.setBusAddress(Objects.requireNonNull(_address, "BusAddress required"));
        return this.self();
    }

    public BusAddress getBusAddress() {
        return this.config.getBusAddress();
    }

    public X withPreConnectCallback(Consumer<AbstractTransport> _callback) {
        this.config.setPreConnectCallback(_callback);
        return this.self();
    }

    public X withAfterBindCallback(Consumer<AbstractTransport> _callback) {
        this.config.setAfterBindCallback(_callback);
        return this.self();
    }

    public X withAutoConnect(boolean _connect) {
        this.config.setAutoConnect(_connect);
        return this.self();
    }

    public X withRegisterSelf(boolean _register) {
        this.config.setRegisterSelf(_register);
        return this.self();
    }

    public SaslConfigBuilder<R> configureSasl() {
        return this.saslConfigBuilder;
    }

    public X withListening(boolean _listen) {
        this.config.setListening(_listen);
        return this.self();
    }

    public X withTimeout(int _timeout) {
        if (_timeout >= 0) {
            this.config.setTimeout(_timeout);
        }
        return this.self();
    }

    public X withUnixSocketFileOwner(String _user) {
        this.config.setFileOwner(_user);
        return this.self();
    }

    public X withUnixSocketFileGroup(String _group) {
        this.config.setFileGroup(_group);
        return this.self();
    }

    public X withUnixSocketFilePermissions(PosixFilePermission ... _permissions) {
        this.config.setFileUnixPermissions(_permissions);
        return this.self();
    }

    public X withAdditionalConfig(String _key, Object _value) {
        this.config.getAdditionalConfig().put(_key, _value);
        return this.self();
    }

    public X withRemoveAdditionalConfig(String _key) {
        this.config.getAdditionalConfig().remove(_key);
        return this.self();
    }

    public X withEndianess(byte _endianess) {
        if (_endianess == 66 || _endianess == 108) {
            this.config.setEndianess(_endianess);
        }
        return this.self();
    }

    public R back() {
        return this.connectionBuilder != null ? (R)this.connectionBuilder.get() : null;
    }

    public TransportConfig build() {
        SaslConfig saslCfg = this.saslConfigBuilder.build();
        this.config.setSaslConfig(saslCfg);
        return this.config;
    }
}

