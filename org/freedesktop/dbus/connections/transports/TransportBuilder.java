
package org.freedesktop.dbus.connections.transports;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.connections.config.TransportConfigBuilder;
import org.freedesktop.dbus.connections.transports.AbstractTransport;
import org.freedesktop.dbus.connections.transports.IFileBasedBusAddress;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.InvalidBusAddressException;
import org.freedesktop.dbus.exceptions.TransportConfigurationException;
import org.freedesktop.dbus.exceptions.TransportRegistrationException;
import org.freedesktop.dbus.spi.transport.ITransportProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransportBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportBuilder.class);
    private static final Map<String, ITransportProvider> PROVIDERS = TransportBuilder.getTransportProvider();
    private TransportConfigBuilder<TransportConfigBuilder<?, TransportBuilder>, TransportBuilder> transportConfigBuilder = new TransportConfigBuilder(() -> this);

    private TransportBuilder(TransportConfig _config) {
        if (_config != null) {
            this.transportConfigBuilder.withConfig(_config);
        }
    }

    static Map<String, ITransportProvider> getTransportProvider() {
        ConcurrentHashMap<String, ITransportProvider> providers = new ConcurrentHashMap<String, ITransportProvider>();
        try {
            ServiceLoader<ITransportProvider> spiLoader = ServiceLoader.load(ITransportProvider.class, TransportBuilder.class.getClassLoader());
            for (ITransportProvider provider : spiLoader) {
                String providerBusType = provider.getSupportedBusType();
                if (providerBusType == null) {
                    LOGGER.warn("Transport {} is invalid: No bustype configured", (Object)provider.getClass());
                    continue;
                }
                providerBusType = providerBusType.toUpperCase(Locale.US);
                LOGGER.debug("Found provider '{}' named '{}' providing bustype '{}'", provider.getClass().getSimpleName(), provider.getTransportName(), providerBusType);
                if (providers.containsKey(providerBusType)) {
                    throw new TransportRegistrationException("Found transport " + ((ITransportProvider)providers.get(providerBusType)).getClass().getName() + " and " + provider.getClass().getName() + " both providing transport for socket type " + providerBusType + ", please only add one of them to classpath.");
                }
                providers.put(providerBusType, provider);
            }
            if (providers.isEmpty()) {
                throw new TransportRegistrationException("No dbus-java-transport found in classpath, please add a transport module");
            }
        }
        catch (ServiceConfigurationError _ex) {
            LOGGER.error("Could not initialize service provider.", _ex);
        }
        return providers;
    }

    public static TransportBuilder create(String _address) throws InvalidBusAddressException {
        TransportConfig cfg = new TransportConfig();
        cfg.setBusAddress(BusAddress.of(_address));
        return new TransportBuilder(cfg);
    }

    public static TransportBuilder create(TransportConfig _config) throws InvalidBusAddressException {
        return new TransportBuilder(_config);
    }

    public static TransportBuilder create() {
        return new TransportBuilder(null);
    }

    public static TransportBuilder create(BusAddress _address) {
        Objects.requireNonNull(_address, "BusAddress required");
        return new TransportBuilder(new TransportConfig(_address));
    }

    public static TransportBuilder createWithDynamicSession(String _transportType) throws DBusException {
        String dynSession = TransportBuilder.createDynamicSession(_transportType, false);
        if (dynSession == null) {
            throw new DBusException("Could not create dynamic session for transport type '" + _transportType + "'");
        }
        return TransportBuilder.create(dynSession);
    }

    public TransportConfigBuilder<TransportConfigBuilder<?, TransportBuilder>, TransportBuilder> configure() {
        return this.transportConfigBuilder;
    }

    public AbstractTransport build() throws DBusException, IOException {
        BusAddress myBusAddress = this.getAddress();
        TransportConfig config = this.transportConfigBuilder.build();
        if (myBusAddress == null) {
            throw new DBusException("Transport requires a BusAddress, use withBusAddress() to configure before building");
        }
        int configuredSaslAuthMode = config.getSaslConfig().getAuthMode();
        AbstractTransport transport = null;
        ITransportProvider provider = PROVIDERS.get(config.getBusAddress().getBusType());
        if (provider == null) {
            throw new DBusException("No transport provider found for bustype " + config.getBusAddress().getBusType());
        }
        LOGGER.info("Using transport {} for address {}", (Object)provider.getTransportName(), (Object)config.getBusAddress());
        try {
            transport = provider.createTransport(myBusAddress, config);
            Objects.requireNonNull(transport, "Transport required");
            if (configuredSaslAuthMode > 0 && config.getSaslConfig().getAuthMode() != configuredSaslAuthMode) {
                transport.getSaslConfig().setAuthMode(configuredSaslAuthMode);
            }
        }
        catch (TransportConfigurationException _ex) {
            LOGGER.error("Could not initialize transport", _ex);
        }
        if (transport == null) {
            throw new DBusException("Unknown address type " + myBusAddress.getType() + " or no transport provider found for bus type " + myBusAddress.getBusType());
        }
        if (myBusAddress.isListeningSocket() && myBusAddress instanceof IFileBasedBusAddress) {
            IFileBasedBusAddress fbba = (IFileBasedBusAddress)((Object)myBusAddress);
            fbba.updatePermissions(config.getFileOwner(), config.getFileGroup(), config.getFileUnixPermissions());
        }
        transport.setPreConnectCallback(config.getPreConnectCallback());
        if (config.isAutoConnect() && !config.isListening()) {
            SocketChannel c = null;
            int max = Math.max(500, config.getTimeout()) / 500;
            int cnt = 0;
            do {
                try {
                    ++cnt;
                    c = transport.connect();
                }
                catch (IOException _ex) {
                    LOGGER.debug("Connection to {} failed, reconnect attempt {} of {}", this.getAddress(), cnt, max);
                    if (cnt >= max) {
                        throw _ex;
                    }
                    try {
                        Thread.sleep(500L);
                    }
                    catch (InterruptedException _ex1) {
                        LOGGER.debug("Interrupted while waiting for connection retry for address {}", (Object)this.getAddress());
                        Thread.currentThread().interrupt();
                    }
                }
            } while (c == null);
            LOGGER.debug("Connection to {} established after {} of {} attempts", this.getAddress(), cnt, max);
        }
        return transport;
    }

    public BusAddress getAddress() {
        return this.configure().getBusAddress();
    }

    public static List<String> getRegisteredBusTypes() {
        return new ArrayList<String>(PROVIDERS.keySet());
    }

    public static String createDynamicSession(String _busType, boolean _listeningAddress) {
        Objects.requireNonNull(_busType, "Bustype required");
        ITransportProvider provider = PROVIDERS.get(_busType.toUpperCase(Locale.US));
        if (provider != null) {
            return provider.createDynamicSessionAddress(_listeningAddress);
        }
        return null;
    }

    public static enum SaslAuthMode {
        AUTH_ANONYMOUS(4),
        AUTH_COOKIE(2),
        AUTH_EXTERNAL(1);

        private final int authMode;

        private SaslAuthMode(int _authMode) {
            this.authMode = _authMode;
        }

        public int getAuthMode() {
            return this.authMode;
        }
    }
}

