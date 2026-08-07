
package org.freedesktop.dbus.bin;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.bin.EmbeddedDBusDaemon;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.transports.AbstractTransport;
import org.freedesktop.dbus.connections.transports.TransportBuilder;
import org.freedesktop.dbus.connections.transports.TransportConnection;
import org.freedesktop.dbus.errors.AccessDenied;
import org.freedesktop.dbus.errors.MatchRuleInvalid;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.interfaces.FatalException;
import org.freedesktop.dbus.interfaces.Introspectable;
import org.freedesktop.dbus.interfaces.Peer;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.messages.MessageFactory;
import org.freedesktop.dbus.messages.MethodCall;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.freedesktop.dbus.utils.AddressBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBusDaemon
extends Thread
implements Closeable {
    public static final int QUEUE_POLL_WAIT = 500;
    private static final String DBUS_BUSPATH = "/org/freedesktop/DBus";
    private static final String DBUS_BUSNAME = "org.freedesktop.DBus";
    private static final Logger LOGGER = LoggerFactory.getLogger(DBusDaemon.class);
    private final Map<ConnectionStruct, DBusDaemonReaderThread> conns = new ConcurrentHashMap<ConnectionStruct, DBusDaemonReaderThread>();
    private final Map<String, ConnectionStruct> names = Collections.synchronizedMap(new HashMap());
    private final BlockingDeque<Pair<Message, WeakReference<ConnectionStruct>>> outqueue = new LinkedBlockingDeque<Pair<Message, WeakReference<ConnectionStruct>>>();
    private final BlockingDeque<Pair<Message, WeakReference<ConnectionStruct>>> inqueue = new LinkedBlockingDeque<Pair<Message, WeakReference<ConnectionStruct>>>();
    private final List<ConnectionStruct> sigrecips = new ArrayList<ConnectionStruct>();
    private final DBusServer dbusServer = new DBusServer();
    private final DBusDaemonSenderThread sender = new DBusDaemonSenderThread();
    private final AtomicBoolean run = new AtomicBoolean(false);
    private final AtomicInteger nextUnique = new AtomicInteger(0);
    private final AbstractTransport transport;

    public DBusDaemon(AbstractTransport _transport) {
        this.setName(this.getClass().getSimpleName() + "-Thread");
        this.transport = _transport;
        this.names.put(DBUS_BUSNAME, null);
    }

    private void send(ConnectionStruct _connStruct, Message _msg) {
        this.send(_connStruct, _msg, false);
    }

    private void send(ConnectionStruct _connStruct, Message _msg, boolean _head) {
        if (_connStruct == null) {
            LOGGER.trace("Queuing message {} for all connections", (Object)_msg);
            for (ConnectionStruct d : this.conns.keySet()) {
                if (d.connection == null || d.connection.getChannel() == null || !d.connection.getChannel().isConnected()) {
                    LOGGER.debug("Ignoring broadcast message for disconnected connection {}: {}", (Object)d.connection, (Object)_msg);
                    continue;
                }
                if (_head) {
                    this.outqueue.addFirst(new Pair<Message, WeakReference<ConnectionStruct>>(_msg, new WeakReference<ConnectionStruct>(d)));
                    continue;
                }
                this.outqueue.addLast(new Pair<Message, WeakReference<ConnectionStruct>>(_msg, new WeakReference<ConnectionStruct>(d)));
            }
        } else {
            LOGGER.trace("Queuing message {} for {}", (Object)_msg, (Object)_connStruct.unique);
            if (_head) {
                this.outqueue.addFirst(new Pair<Message, WeakReference<ConnectionStruct>>(_msg, new WeakReference<ConnectionStruct>(_connStruct)));
            } else {
                this.outqueue.addLast(new Pair<Message, WeakReference<ConnectionStruct>>(_msg, new WeakReference<ConnectionStruct>(_connStruct)));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        this.run.set(true);
        this.sender.start();
        while (this.isRunning()) {
            try {
                Pair<Message, WeakReference<ConnectionStruct>> pollFirst = this.inqueue.take();
                ConnectionStruct connectionStruct = (ConnectionStruct)((WeakReference)pollFirst.second).get();
                if (connectionStruct == null) continue;
                Message m = (Message)pollFirst.first;
                DBusDaemon.logMessage("<inqueue> Got message {} from {}", m, connectionStruct.unique);
                MessageFactory messageFactory = connectionStruct.connection.getMessageFactory();
                if (!(null != connectionStruct.unique || m instanceof MethodCall && DBUS_BUSNAME.equals(m.getDestination()) && "Hello".equals(m.getName()))) {
                    this.send(connectionStruct, messageFactory.createError(DBUS_BUSNAME, null, "org.freedesktop.DBus.Error.AccessDenied", m.getSerial(), "s", "You must send a Hello message"));
                    continue;
                }
                try {
                    if (null != connectionStruct.unique) {
                        m.setSource(connectionStruct.unique);
                        LOGGER.trace("Updated source to {}", (Object)connectionStruct.unique);
                    }
                }
                catch (DBusException _ex) {
                    LOGGER.debug("Error setting source", _ex);
                    this.send(connectionStruct, messageFactory.createError(DBUS_BUSNAME, null, "org.freedesktop.DBus.Error.GeneralError", m.getSerial(), "s", "Sending message failed"));
                }
                if (DBUS_BUSNAME.equals(m.getDestination())) {
                    this.dbusServer.handleMessage(connectionStruct, (Message)pollFirst.first);
                    continue;
                }
                if (m instanceof DBusSignal) {
                    ArrayList<ConnectionStruct> l;
                    List<ConnectionStruct> list = this.sigrecips;
                    synchronized (list) {
                        l = new ArrayList<ConnectionStruct>(this.sigrecips);
                    }
                    ArrayList<ConnectionStruct> list2 = l;
                    for (ConnectionStruct d : list2) {
                        this.send(d, m);
                    }
                    continue;
                }
                ConnectionStruct dest = this.names.get(m.getDestination());
                if (null == dest) {
                    this.send(connectionStruct, messageFactory.createError(DBUS_BUSNAME, null, "org.freedesktop.DBus.Error.ServiceUnknown", m.getSerial(), "s", String.format("The name `%s' does not exist", m.getDestination())));
                    continue;
                }
                this.send(dest, m);
            }
            catch (DBusException _ex) {
                LOGGER.debug("Error processing connection", _ex);
            }
            catch (InterruptedException _ex) {
                LOGGER.debug("Interrupted");
                this.close();
                this.interrupt();
            }
        }
    }

    private static void logMessage(String _logStr, Message _m, String _connUniqueId) {
        Object logMsg = _m;
        if (_m != null && Introspectable.class.getName().equals(_m.getInterface()) && !LOGGER.isTraceEnabled()) {
            logMsg = "<Introspection data only visible in loglevel trace>";
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(_logStr, logMsg, (Object)_connUniqueId);
        } else {
            LOGGER.debug(_logStr, (Object)_m, (Object)_connUniqueId);
        }
    }

    public synchronized boolean isRunning() {
        return this.run.get();
    }

    @Override
    public void close() {
        this.run.set(false);
        if (!this.conns.isEmpty()) {
            HashSet<ConnectionStruct> connections = new HashSet<ConnectionStruct>(this.conns.keySet());
            for (ConnectionStruct c : connections) {
                this.removeConnection(c);
            }
        }
        this.sender.terminate();
        if (this.transport != null) {
            LOGGER.debug("Terminating transport {}", (Object)this.transport);
            try {
                this.transport.close();
            }
            catch (IOException _ex) {
                LOGGER.debug("Error closing transport", _ex);
            }
        }
        this.interrupt();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeConnection(ConnectionStruct _c) {
        DBusDaemonReaderThread oldThread = this.conns.remove(_c);
        if (oldThread != null) {
            LOGGER.debug("Terminating reader thread for {}", (Object)_c);
            oldThread.terminate();
            try {
                if (_c.connection != null) {
                    _c.connection.close();
                    LOGGER.debug("Terminated connection {}", (Object)_c.connection);
                }
            }
            catch (IOException _exIo) {
                LOGGER.debug("Error while closing socketchannel", _exIo);
            }
        }
        LOGGER.debug("Removing signal destination {}", (Object)_c);
        Object object = this.sigrecips;
        synchronized (object) {
            if (this.sigrecips.removeIf(e -> e.equals(_c))) {
                LOGGER.debug("Removed one or more signal destinations for {}", (Object)_c);
            }
        }
        LOGGER.debug("Removing name registration for {}", (Object)_c);
        object = this.names;
        synchronized (object) {
            ArrayList<String> toRemove = new ArrayList<String>();
            for (Map.Entry<String, ConnectionStruct> e2 : this.names.entrySet()) {
                if (e2.getValue() != _c) continue;
                toRemove.add(e2.getKey());
            }
            for (String name : toRemove) {
                this.names.remove(name);
                try {
                    this.send(null, new DBus.NameOwnerChanged(DBUS_BUSPATH, name, _c.unique, ""));
                }
                catch (DBusException _ex) {
                    LOGGER.debug("Unable to change owner", _ex);
                }
            }
        }
    }

    void addSock(TransportConnection _s) {
        LOGGER.debug("New Client");
        ConnectionStruct c = new ConnectionStruct(_s);
        DBusDaemonReaderThread r = new DBusDaemonReaderThread(c);
        this.conns.put(c, r);
        r.start();
    }

    public static void syntax() {
        System.out.println("Syntax: DBusDaemon [--version] [-v] [--help] [-h] [--listen address] [-l address] [--print-address] [-r] [--pidfile file] [-p file] [--addressfile file] [--auth-mode AUTH_ANONYMOUS|AUTH_COOKIE|AUTH_EXTERNAL] [-m AUTH_ANONYMOUS|AUTH_COOKIE|AUTH_EXTERNAL][-a file] [--unix] [-u] [--tcp] [-t] ");
        System.exit(1);
    }

    public static void version() {
        System.out.println("D-Bus Java Version: " + System.getProperty("Version"));
        System.exit(1);
    }

    public static void saveFile(String _data, String _file) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileOutputStream(_file));){
            w.println(_data);
        }
    }

    public static void main(String[] _args) throws Exception {
        String addr = null;
        String pidfile = null;
        String addrfile = null;
        String authModeStr = null;
        boolean printaddress = false;
        boolean unix = true;
        boolean tcp = false;
        try {
            for (int i = 0; i < _args.length; ++i) {
                if ("--help".equals(_args[i]) || "-h".equals(_args[i])) {
                    DBusDaemon.syntax();
                    continue;
                }
                if ("--version".equals(_args[i]) || "-v".equals(_args[i])) {
                    DBusDaemon.version();
                    continue;
                }
                if ("--listen".equals(_args[i]) || "-l".equals(_args[i])) {
                    addr = _args[++i];
                    continue;
                }
                if ("--pidfile".equals(_args[i]) || "-p".equals(_args[i])) {
                    pidfile = _args[++i];
                    continue;
                }
                if ("--addressfile".equals(_args[i]) || "-a".equals(_args[i])) {
                    addrfile = _args[++i];
                    continue;
                }
                if ("--print-address".equals(_args[i]) || "-r".equals(_args[i])) {
                    printaddress = true;
                    continue;
                }
                if ("--unix".equals(_args[i]) || "-u".equals(_args[i])) {
                    unix = true;
                    tcp = false;
                    continue;
                }
                if ("--tcp".equals(_args[i]) || "-t".equals(_args[i])) {
                    tcp = true;
                    unix = false;
                    continue;
                }
                if ("--auth-mode".equals(_args[i]) || "-m".equals(_args[i])) {
                    authModeStr = _args[++i];
                    continue;
                }
                DBusDaemon.syntax();
            }
        }
        catch (ArrayIndexOutOfBoundsException _ex) {
            DBusDaemon.syntax();
        }
        if (null == addr && unix) {
            addr = TransportBuilder.createDynamicSession("UNIX", true);
        } else if (null == addr && tcp) {
            addr = TransportBuilder.createDynamicSession("TCP", true);
        }
        BusAddress address = BusAddress.of(addr);
        if (printaddress) {
            System.out.println(addr);
        }
        TransportBuilder.SaslAuthMode saslAuthMode = null;
        if (authModeStr != null) {
            String selectedMode = authModeStr;
            saslAuthMode = Arrays.stream(TransportBuilder.SaslAuthMode.values()).filter(e -> e.name().toLowerCase().matches(selectedMode.toLowerCase())).findFirst().orElseThrow(() -> new IllegalArgumentException("Auth mode '" + selectedMode + "' unsupported"));
        }
        if (null != addrfile) {
            DBusDaemon.saveFile(addr, addrfile);
        }
        if (null != pidfile) {
            DBusDaemon.saveFile(System.getProperty("Pid"), pidfile);
        }
        LOGGER.info("Binding to {}", (Object)addr);
        try (EmbeddedDBusDaemon daemon = new EmbeddedDBusDaemon(address);){
            daemon.setSaslAuthMode(saslAuthMode);
            daemon.startInForeground();
        }
    }

    public class DBusServer
    implements DBus,
    Introspectable,
    Peer {
        private final String machineId = AddressBuilder.createMachineId();
        private ConnectionStruct connStruct;

        private DBusSignal generateNameAcquiredSignal(TransportConnection _connection, String _name) throws DBusException {
            return _connection.getMessageFactory().createSignal(DBusDaemon.DBUS_BUSNAME, DBusDaemon.DBUS_BUSPATH, DBusDaemon.DBUS_BUSNAME, "NameAcquired", "s", _name);
        }

        private DBusSignal generatedNameOwnerChangedSignal(TransportConnection _connection, String _name, String _oldOwner, String _newOwner) throws DBusException {
            return _connection.getMessageFactory().createSignal(DBusDaemon.DBUS_BUSNAME, DBusDaemon.DBUS_BUSPATH, DBusDaemon.DBUS_BUSNAME, "NameOwnerChanged", "sss", _name, _oldOwner, _newOwner);
        }

        @Override
        public boolean isRemote() {
            return false;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String Hello() {
            ConnectionStruct connectionStruct = this.connStruct;
            synchronized (connectionStruct) {
                if (null != this.connStruct.unique) {
                    throw new AccessDenied("Connection has already sent a Hello message");
                }
                this.connStruct.unique = ":1." + DBusDaemon.this.nextUnique.incrementAndGet();
            }
            DBusDaemon.this.names.put(this.connStruct.unique, this.connStruct);
            LOGGER.info("Client {} registered", (Object)this.connStruct.unique);
            try {
                DBusDaemon.this.send(this.connStruct, this.generateNameAcquiredSignal(this.connStruct.connection, this.connStruct.unique));
                DBusDaemon.this.send(null, this.generatedNameOwnerChangedSignal(this.connStruct.connection, this.connStruct.unique, "", this.connStruct.unique));
            }
            catch (DBusException _ex) {
                LOGGER.debug("", _ex);
            }
            return this.connStruct.unique;
        }

        @Override
        public String[] ListNames() {
            Set<String> nss = DBusDaemon.this.names.keySet();
            String[] ns = nss.toArray(new String[0]);
            return ns;
        }

        @Override
        public boolean NameHasOwner(String _name) {
            return DBusDaemon.this.names.containsKey(_name);
        }

        @Override
        public String GetNameOwner(String _name) {
            ConnectionStruct owner = DBusDaemon.this.names.get(_name);
            String o = null == owner ? "" : owner.unique;
            return o;
        }

        @Override
        public UInt32 GetConnectionUnixUser(String _connectionName) {
            return new UInt32(0L);
        }

        @Override
        public UInt32 StartServiceByName(String _name, UInt32 _flags) {
            return new UInt32(0L);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public UInt32 RequestName(String _name, UInt32 _flags) {
            int rv;
            boolean exists = false;
            Map<String, ConnectionStruct> map = DBusDaemon.this.names;
            synchronized (map) {
                exists = DBusDaemon.this.names.containsKey(_name);
                if (!exists) {
                    DBusDaemon.this.names.put(_name, this.connStruct);
                }
            }
            if (exists) {
                rv = 3;
            } else {
                LOGGER.info("Client {} acquired name {}", (Object)this.connStruct.unique, (Object)_name);
                rv = 1;
                try {
                    DBusDaemon.this.send(this.connStruct, this.generateNameAcquiredSignal(this.connStruct.connection, _name));
                    DBusDaemon.this.send(null, this.generatedNameOwnerChangedSignal(this.connStruct.connection, _name, "", this.connStruct.unique));
                }
                catch (DBusException _ex) {
                    LOGGER.debug("", _ex);
                }
            }
            return new UInt32(rv);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public UInt32 ReleaseName(String _name) {
            int rv;
            boolean exists = false;
            Map<String, ConnectionStruct> map = DBusDaemon.this.names;
            synchronized (map) {
                if (DBusDaemon.this.names.containsKey(_name) && DBusDaemon.this.names.get(_name).equals(this.connStruct)) {
                    exists = DBusDaemon.this.names.remove(_name) != null;
                }
            }
            if (!exists) {
                rv = 2;
            } else {
                LOGGER.info("Client {} acquired name {}", (Object)this.connStruct.unique, (Object)_name);
                rv = 1;
                try {
                    DBusDaemon.this.send(this.connStruct, new DBus.NameLost(DBusDaemon.DBUS_BUSPATH, _name));
                    DBusDaemon.this.send(null, new DBus.NameOwnerChanged(DBusDaemon.DBUS_BUSPATH, _name, this.connStruct.unique, ""));
                }
                catch (DBusException _ex) {
                    LOGGER.debug("", _ex);
                }
            }
            return new UInt32(rv);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void AddMatch(String _matchrule) throws MatchRuleInvalid {
            LOGGER.trace("Adding match rule: {}", (Object)_matchrule);
            List<ConnectionStruct> list = DBusDaemon.this.sigrecips;
            synchronized (list) {
                if (!DBusDaemon.this.sigrecips.contains(this.connStruct)) {
                    DBusDaemon.this.sigrecips.add(this.connStruct);
                }
            }
        }

        @Override
        public void RemoveMatch(String _matchrule) throws MatchRuleInvalid {
            LOGGER.trace("Removing match rule: {}", (Object)_matchrule);
        }

        @Override
        public String[] ListQueuedOwners(String _name) {
            return new String[0];
        }

        @Override
        public UInt32 GetConnectionUnixProcessID(String _connectionName) {
            return new UInt32(0L);
        }

        @Override
        public Byte[] GetConnectionSELinuxSecurityContext(String _args) {
            return new Byte[0];
        }

        private void handleMessage(ConnectionStruct _connStruct, Message _msg) throws DBusException {
            block9: {
                LOGGER.trace("Handling message {}  from {}", (Object)_msg, (Object)_connStruct.unique);
                if (!(_msg instanceof MethodCall)) {
                    return;
                }
                Object[] args = _msg.getParameters();
                Class[] cs = new Class[args.length];
                for (int i = 0; i < cs.length; ++i) {
                    cs[i] = args[i].getClass();
                }
                Method meth = null;
                Object rv = null;
                MessageFactory messageFactory = _connStruct.connection.getMessageFactory();
                try {
                    meth = DBusServer.class.getMethod(_msg.getName(), cs);
                    try {
                        this.connStruct = _connStruct;
                        rv = meth.invoke((Object)DBusDaemon.this.dbusServer, args);
                        if (null == rv) {
                            DBusDaemon.this.send(_connStruct, messageFactory.createMethodReturn(DBusDaemon.DBUS_BUSNAME, (MethodCall)_msg, null, new Object[0]), true);
                            break block9;
                        }
                        String sig = Marshalling.getDBusType(meth.getGenericReturnType())[0];
                        DBusDaemon.this.send(_connStruct, messageFactory.createMethodReturn(DBusDaemon.DBUS_BUSNAME, (MethodCall)_msg, sig, rv), true);
                    }
                    catch (InvocationTargetException _exIte) {
                        LOGGER.debug("", _exIte);
                        DBusDaemon.this.send(_connStruct, messageFactory.createError(DBusDaemon.DBUS_BUSNAME, _msg, _exIte.getCause()));
                    }
                    catch (DBusExecutionException _exDnEe) {
                        LOGGER.debug("", _exDnEe);
                        DBusDaemon.this.send(_connStruct, messageFactory.createError(DBusDaemon.DBUS_BUSNAME, _msg, _exDnEe));
                    }
                    catch (Exception _ex) {
                        LOGGER.debug("", _ex);
                        DBusDaemon.this.send(_connStruct, messageFactory.createError(DBusDaemon.DBUS_BUSNAME, _connStruct.unique, "org.freedesktop.DBus.Error.GeneralError", _msg.getSerial(), "s", "An error occurred while calling " + _msg.getName()));
                    }
                }
                catch (NoSuchMethodException _exNsm) {
                    DBusDaemon.this.send(_connStruct, messageFactory.createError(DBusDaemon.DBUS_BUSNAME, _connStruct.unique, "org.freedesktop.DBus.Error.UnknownMethod", _msg.getSerial(), "s", "This service does not support " + _msg.getName()));
                }
            }
        }

        @Override
        public String getObjectPath() {
            return null;
        }

        @Override
        public String Introspect() {
            return "<!DOCTYPE node PUBLIC \"-
        }

        @Override
        public void Ping() {
        }

        @Override
        public String[] ListActivatableNames() {
            return null;
        }

        @Override
        public Map<String, Variant<?>> GetConnectionCredentials(String _busName) {
            return null;
        }

        @Override
        public Byte[] GetAdtAuditSessionData(String _busName) {
            return null;
        }

        @Override
        public void UpdateActivationEnvironment(Map<String, String>[] _environment) {
        }

        @Override
        public String GetId() {
            return null;
        }

        @Override
        public String GetMachineId() {
            return this.machineId;
        }
    }

    public class DBusDaemonSenderThread
    extends Thread {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private final AtomicBoolean running = new AtomicBoolean(false);

        public DBusDaemonSenderThread() {
            this.setName(this.getClass().getSimpleName().replace('$', '-'));
        }

        @Override
        public void run() {
            this.logger.debug(">>>> Sender thread started <<<<");
            this.running.set(true);
            while (DBusDaemon.this.isRunning() && this.running.get()) {
                this.logger.trace("Acquiring lock on outqueue and blocking for data");
                try {
                    Pair<Message, WeakReference<ConnectionStruct>> pollFirst = DBusDaemon.this.outqueue.take();
                    if (pollFirst == null) continue;
                    ConnectionStruct connectionStruct = (ConnectionStruct)((WeakReference)pollFirst.second).get();
                    if (connectionStruct != null) {
                        if (connectionStruct.connection.getChannel().isConnected()) {
                            this.logger.debug("<outqueue> Got message {} for {}", pollFirst.first, (Object)connectionStruct.unique);
                            try {
                                connectionStruct.connection.getWriter().writeMessage((Message)pollFirst.first);
                            }
                            catch (IOException _ex) {
                                this.logger.debug("Disconnecting client due to previous exception", _ex);
                                DBusDaemon.this.removeConnection(connectionStruct);
                            }
                            continue;
                        }
                        this.logger.warn("Connection to {} broken", (Object)connectionStruct.connection);
                        DBusDaemon.this.removeConnection(connectionStruct);
                        continue;
                    }
                    this.logger.info("Discarding {} connection reaped", pollFirst.first);
                }
                catch (InterruptedException _ex) {
                    this.logger.debug("Got interrupted", _ex);
                    Thread.currentThread().interrupt();
                }
            }
            this.logger.debug(">>>> Sender Thread terminated <<<<");
        }

        public synchronized void terminate() {
            this.running.set(false);
            this.interrupt();
        }
    }

    public static class ConnectionStruct {
        private final TransportConnection connection;
        private String unique;

        ConnectionStruct(TransportConnection _c) {
            this.connection = _c;
        }

        public String toString() {
            return null == this.unique ? ":?-?" : this.unique;
        }
    }

    static class Pair<A, B> {
        private final A first;
        private final B second;

        Pair(A _first, B _second) {
            this.first = _first;
            this.second = _second;
        }

        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        public boolean equals(Object _obj) {
            if (this == _obj) {
                return true;
            }
            if (!(_obj instanceof Pair)) {
                return false;
            }
            Pair other = (Pair)_obj;
            return Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second);
        }
    }

    public class DBusDaemonReaderThread
    extends Thread {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private ConnectionStruct conn;
        private final WeakReference<ConnectionStruct> weakconn;
        private final AtomicBoolean running = new AtomicBoolean(false);

        public DBusDaemonReaderThread(ConnectionStruct _conn) {
            this.conn = _conn;
            this.weakconn = new WeakReference<ConnectionStruct>(_conn);
            this.setName(this.getClass().getSimpleName());
        }

        public void terminate() {
            this.running.set(false);
        }

        @Override
        public void run() {
            this.logger.debug(">>>> Reader Thread started <<<<");
            this.running.set(true);
            while (DBusDaemon.this.isRunning() && this.running.get()) {
                Message m;
                block4: {
                    m = null;
                    try {
                        m = this.conn.connection.getReader().readMessage();
                    }
                    catch (IOException _ex) {
                        LOGGER.debug("Error reading message", _ex);
                        DBusDaemon.this.removeConnection(this.conn);
                    }
                    catch (DBusException _ex) {
                        LOGGER.debug("", _ex);
                        if (!(_ex instanceof FatalException)) break block4;
                        DBusDaemon.this.removeConnection(this.conn);
                    }
                }
                if (null == m) continue;
                DBusDaemon.logMessage("Read {} from {}", m, this.conn.unique);
                DBusDaemon.this.inqueue.add(new Pair<Message, WeakReference<ConnectionStruct>>(m, this.weakconn));
            }
            this.conn = null;
            this.logger.debug(">>>> Reader Thread terminated <<<<");
        }
    }
}

