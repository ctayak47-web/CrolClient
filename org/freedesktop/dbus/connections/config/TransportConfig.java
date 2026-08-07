
package org.freedesktop.dbus.connections.config;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.config.SaslConfig;
import org.freedesktop.dbus.connections.impl.BaseConnectionBuilder;
import org.freedesktop.dbus.connections.transports.AbstractTransport;
import org.freedesktop.dbus.utils.Util;

public final class TransportConfig {
    private SaslConfig saslConfig;
    private BusAddress busAddress;
    private Consumer<AbstractTransport> preConnectCallback;
    private Consumer<AbstractTransport> afterBindCallback;
    private int timeout = 10000;
    private boolean autoConnect = true;
    private String fileOwner;
    private String fileGroup;
    private byte endianess = BaseConnectionBuilder.getSystemEndianness();
    private boolean registerSelf = true;
    private Set<PosixFilePermission> fileUnixPermissions;
    private Map<String, Object> additionalConfig = new LinkedHashMap<String, Object>();

    public TransportConfig(BusAddress _address) {
        this.busAddress = _address;
    }

    public TransportConfig() {
        this(null);
    }

    public BusAddress getBusAddress() {
        return this.busAddress;
    }

    public void setBusAddress(BusAddress _busAddress) {
        this.busAddress = Objects.requireNonNull(_busAddress, "BusAddress required");
    }

    public void setListening(boolean _listen) {
        this.updateBusAddress(_listen);
    }

    public boolean isListening() {
        return this.busAddress != null && this.busAddress.isListeningSocket();
    }

    public Consumer<AbstractTransport> getPreConnectCallback() {
        return this.preConnectCallback;
    }

    public void setPreConnectCallback(Consumer<AbstractTransport> _preConnectCallback) {
        this.preConnectCallback = _preConnectCallback;
    }

    public Consumer<AbstractTransport> getAfterBindCallback() {
        return this.afterBindCallback;
    }

    public void setAfterBindCallback(Consumer<AbstractTransport> _afterBindCallback) {
        this.afterBindCallback = _afterBindCallback;
    }

    public boolean isAutoConnect() {
        return this.autoConnect;
    }

    public void setAutoConnect(boolean _autoConnect) {
        this.autoConnect = _autoConnect;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int _timeout) {
        this.timeout = _timeout;
    }

    public String getFileOwner() {
        return this.fileOwner;
    }

    public void setFileOwner(String _fileOwner) {
        this.fileOwner = _fileOwner;
    }

    public String getFileGroup() {
        return this.fileGroup;
    }

    public void setFileGroup(String _fileGroup) {
        this.fileGroup = _fileGroup;
    }

    public Set<PosixFilePermission> getFileUnixPermissions() {
        return this.fileUnixPermissions;
    }

    public void setFileUnixPermissions(PosixFilePermission ... _permissions) {
        if (Util.isWindows()) {
            return;
        }
        if (_permissions == null || _permissions.length < 1) {
            return;
        }
        this.fileUnixPermissions = new LinkedHashSet<PosixFilePermission>(Arrays.asList(_permissions));
    }

    public Map<String, Object> getAdditionalConfig() {
        return this.additionalConfig;
    }

    public void setAdditionalConfig(Map<String, Object> _additionalConfig) {
        this.additionalConfig = _additionalConfig;
    }

    public SaslConfig getSaslConfig() {
        if (this.saslConfig == null) {
            this.saslConfig = new SaslConfig();
        }
        return this.saslConfig;
    }

    void setSaslConfig(SaslConfig _saslCfg) {
        this.saslConfig = _saslCfg;
    }

    public byte getEndianess() {
        return this.endianess;
    }

    public void setEndianess(byte _endianess) {
        this.endianess = _endianess;
    }

    public boolean isRegisterSelf() {
        return this.registerSelf;
    }

    public void setRegisterSelf(boolean _registerSelf) {
        this.registerSelf = _registerSelf;
    }

    void updateBusAddress(boolean _listening) {
        if (this.busAddress == null) {
            return;
        }
        if (!this.busAddress.isListeningSocket() && _listening) {
            this.busAddress.addParameter("listen", "true");
        } else if (this.busAddress.isListeningSocket() && !_listening) {
            this.busAddress.removeParameter("listen");
        }
    }
}

