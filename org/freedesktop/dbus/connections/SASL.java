
package org.freedesktop.dbus.connections;

import com.sun.security.auth.module.UnixSystem;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.freedesktop.dbus.connections.config.SaslConfig;
import org.freedesktop.dbus.connections.transports.AbstractTransport;
import org.freedesktop.dbus.connections.transports.AbstractUnixTransport;
import org.freedesktop.dbus.exceptions.AuthenticationException;
import org.freedesktop.dbus.exceptions.SocketClosedException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.utils.Hexdump;
import org.freedesktop.dbus.utils.LoggingHelper;
import org.freedesktop.dbus.utils.TimeMeasure;
import org.freedesktop.dbus.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SASL {
    public static final int AUTH_NONE = 0;
    public static final int AUTH_EXTERNAL = 1;
    public static final int AUTH_SHA = 2;
    public static final int AUTH_ANON = 4;
    public static final int LOCK_TIMEOUT = 1000;
    public static final int NEW_KEY_TIMEOUT_SECONDS = 300;
    public static final int EXPIRE_KEYS_TIMEOUT_SECONDS = 420;
    public static final int MAX_TIME_TRAVEL_SECONDS = 300;
    public static final int COOKIE_TIMEOUT = 240;
    public static final String COOKIE_CONTEXT = "org_freedesktop_java";
    private static final String AUTH_TYPE_EXTERNAL = "EXTERNAL";
    private static final String AUTH_TYPE_DBUS_COOKIE_SHA1 = "DBUS_COOKIE_SHA1";
    private static final String AUTH_TYPE_ANONYMOUS = "ANONYMOUS";
    private static final String INVALID_CMD_ERR = "Got invalid command";
    private static final int MAX_READ_BYTES = 0x100000;
    private static final Random RANDOM = new Random();
    private static final Collator COL = Collator.getInstance();
    private static final String SYSPROP_USER_HOME;
    private static final String DBUS_TEST_HOME_DIR;
    private static final File DBUS_KEYRINGS_DIR;
    private static final Set<PosixFilePermission> BAD_FILE_PERMISSIONS;
    private String challenge = "";
    private String cookie = "";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean fileDescriptorSupported;
    private final SaslConfig saslConfig;

    public SASL(SaslConfig _saslConfig) {
        this.saslConfig = Objects.requireNonNull(_saslConfig, "Sasl Configuration required");
    }

    private String findCookie(String _context, String _id) throws IOException {
        File keyringDir = DBUS_KEYRINGS_DIR;
        if (!Util.isBlank(DBUS_TEST_HOME_DIR)) {
            keyringDir = new File(DBUS_TEST_HOME_DIR);
        }
        File f = new File(keyringDir, _context);
        long currentTime = System.currentTimeMillis() / 1000L;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));){
            String s = null;
            String lCookie = null;
            while (null != (s = r.readLine())) {
                long timestamp;
                String[] line = s.split(" ");
                if (line.length != 3) continue;
                try {
                    timestamp = Long.parseLong(line[1]);
                }
                catch (NumberFormatException _ex) {
                    continue;
                }
                if (!line[0].equals(_id) || timestamp < 0L || currentTime < timestamp - 300L || currentTime >= timestamp + 420L) continue;
                lCookie = line[2];
                break;
            }
            String string = lCookie;
            return string;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void addCookie(String _context, String _id, long _timestamp, String _cookie) throws IOException {
        Set<PosixFilePermission> currentPermissions;
        File keyringDir = DBUS_KEYRINGS_DIR;
        if (!Util.isBlank(DBUS_TEST_HOME_DIR)) {
            keyringDir = new File(DBUS_TEST_HOME_DIR);
        }
        File cookiefile = new File(keyringDir, _context);
        File lock = new File(keyringDir, _context + ".lock");
        File temp = new File(keyringDir, _context + ".temp");
        if (!keyringDir.exists()) {
            if (!keyringDir.mkdirs()) throw new AuthenticationException("Unable to create keyring directory " + String.valueOf(keyringDir));
            if (!Util.isWindows()) {
                Util.setFilePermissions(keyringDir.toPath(), null, null, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
            }
        } else if (!Util.isWindows() && Util.collectionContainsAny(currentPermissions = Files.getPosixFilePermissions(keyringDir.toPath(), LinkOption.NOFOLLOW_LINKS), BAD_FILE_PERMISSIONS)) {
            if (this.saslConfig.isStrictCookiePermissions()) {
                throw new AuthenticationException("Cannot authenticate using cookies: Permissions of directory " + String.valueOf(lock) + " should be 0700");
            }
            this.logger.warn("DBus keyring directory {} should have permissions 0700", (Object)lock);
        }
        Util.waitFor("Lock file " + String.valueOf(lock), lock::createNewFile, 1000L, 50L);
        ArrayList<Object> lines = new ArrayList<Object>();
        if (cookiefile.exists()) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(cookiefile)));){
                String s = null;
                while (null != (s = r.readLine())) {
                    String[] line = s.split(" ");
                    long time = Long.parseLong(line[1]);
                    if (_timestamp - time >= 240L) continue;
                    lines.add(s);
                }
            }
        }
        lines.add(_id + " " + _timestamp + " " + _cookie);
        Files.writeString(temp.toPath(), (CharSequence)String.join((CharSequence)System.lineSeparator(), lines), Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        if (!temp.renameTo(cookiefile)) {
            if (!cookiefile.delete()) {
                this.logger.warn("Unable to delete cookie file {}", (Object)cookiefile);
            } else if (!temp.renameTo(cookiefile)) {
                this.logger.warn("Unable to rename cookie file {} to {}", (Object)temp, (Object)cookiefile);
            }
        }
        if (lock.delete()) return;
        this.logger.error("Cannot delete lock file {}", (Object)lock);
    }

    private String stupidlyEncode(String _data) {
        return Hexdump.toHex(_data.getBytes(), false);
    }

    private String stupidlyEncode(byte[] _data) {
        return Hexdump.toHex(_data, false);
    }

    private byte getNibble(char _c) {
        return switch (_c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (byte)(_c - 48);
            case 'A', 'B', 'C', 'D', 'E', 'F' -> (byte)(_c - 65 + 10);
            case 'a', 'b', 'c', 'd', 'e', 'f' -> (byte)(_c - 97 + 10);
            default -> 0;
        };
    }

    private String stupidlyDecode(String _data) {
        char[] cs = new char[_data.length()];
        char[] res = new char[cs.length / 2];
        _data.getChars(0, _data.length(), cs, 0);
        int i = 0;
        for (int j = 0; j < res.length; ++j) {
            int b = 0;
            b |= this.getNibble(cs[i]) << 4;
            res[j] = (char)(b |= this.getNibble(cs[i + 1]));
            i += 2;
        }
        return new String(res);
    }

    public Command receive(SocketChannel _sock) throws IOException {
        StringBuilder sb = new StringBuilder();
        ByteBuffer buf = ByteBuffer.allocate(1);
        boolean runLoop = true;
        int bytesRead = 0;
        while (runLoop) {
            int read = _sock.read(buf);
            bytesRead += read;
            buf.position(0);
            if (read == -1) {
                throw new SocketClosedException("Stream unexpectedly short (broken pipe)");
            }
            for (int i = buf.position(); i < read; ++i) {
                byte c = buf.get();
                if (c == 0 || c == 13) continue;
                if (c == 10) {
                    runLoop = false;
                    break;
                }
                sb.append((char)c);
            }
            buf.clear();
            if (bytesRead <= 0x100000) continue;
            break;
        }
        this.logger.trace("received: {}", (Object)sb);
        try {
            return new Command(sb.toString());
        }
        catch (Exception _ex) {
            this.logger.error("Cannot create command.", _ex);
            throw new AuthenticationException("Failed to authenticate.", _ex);
        }
    }

    public void send(SocketChannel _sock, SaslCommand _command, String ... _data) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(_command.name());
        for (String s : _data) {
            sb.append(' ');
            sb.append(s);
        }
        sb.append('\r');
        sb.append('\n');
        this.logger.trace("sending: {}", (Object)sb);
        _sock.write(ByteBuffer.wrap(sb.toString().getBytes()));
    }

    SaslResult doChallenge(int _auth, Command _c) throws IOException {
        switch (_auth) {
            case 2: {
                String[] reply = this.stupidlyDecode(_c.getData()).split(" ");
                LoggingHelper.logIf(this.logger.isTraceEnabled(), () -> this.logger.trace("Auth data: {}", (Object)Arrays.toString(reply)));
                if (3 != reply.length) {
                    this.logger.debug("Reply is not length 3");
                    return SaslResult.ERROR;
                }
                String context = reply[0];
                String id = reply[1];
                String serverchallenge = reply[2];
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA");
                }
                catch (NoSuchAlgorithmException _ex) {
                    this.logger.debug("Could not find SHA algorithm", _ex);
                    return SaslResult.ERROR;
                }
                byte[] buf = new byte[8];
                long seed = Optional.of(System.nanoTime()).map(t -> t < 0L ? t * -1L : t).get();
                Message.marshallintBig(seed, buf, 0, 8);
                String clientchallenge = this.stupidlyEncode(md.digest(buf));
                md.reset();
                TimeMeasure tm = new TimeMeasure();
                String lCookie = null;
                while (lCookie == null && tm.getElapsed() < 1000L) {
                    lCookie = this.findCookie(context, id);
                }
                if (lCookie == null) {
                    this.logger.debug("Did not find a cookie in context {}  with ID {}", (Object)context, (Object)id);
                    return SaslResult.ERROR;
                }
                Object response = serverchallenge + ":" + clientchallenge + ":" + lCookie;
                buf = md.digest(((String)response).getBytes());
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("Response: {} hash: {}", response, (Object)Hexdump.format(buf));
                }
                response = this.stupidlyEncode(buf);
                _c.setResponse(this.stupidlyEncode(clientchallenge + " " + (String)response));
                return SaslResult.OK;
            }
            case 4: {
                _c.setResponse(_c.getData() == null ? "" : _c.getData());
                return SaslResult.OK;
            }
        }
        this.logger.debug("Not DBUS_COOKIE_SHA1 authtype.");
        return SaslResult.ERROR;
    }

    SaslResult doResponse(int _auth, String _uid, String _kernelUid, Command _c) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        }
        catch (NoSuchAlgorithmException _ex) {
            this.logger.error("SHA hash algorithm not available", _ex);
            return SaslResult.ERROR;
        }
        switch (_auth) {
            case 0: {
                switch (_c.getMechs()) {
                    case 4: {
                        return SaslResult.OK;
                    }
                    case 1: {
                        if (0 == COL.compare(_uid, _c.getData()) && (null == _kernelUid || 0 == COL.compare(_uid, _kernelUid))) {
                            return SaslResult.OK;
                        }
                        return SaslResult.REJECT;
                    }
                    case 2: {
                        String context = COOKIE_CONTEXT;
                        long id = System.currentTimeMillis();
                        byte[] buf = new byte[8];
                        Message.marshallintBig(id, buf, 0, 8);
                        this.challenge = this.stupidlyEncode(md.digest(buf));
                        RANDOM.nextBytes(buf);
                        this.cookie = this.stupidlyEncode(md.digest(buf));
                        try {
                            this.addCookie(context, "" + id, id / 1000L, this.cookie);
                        }
                        catch (IOException _ex) {
                            this.logger.error("Error authenticating using cookie", _ex);
                            return SaslResult.ERROR;
                        }
                        this.logger.debug("Sending challenge: {} {} {}", context, id, this.challenge);
                        _c.setResponse(this.stupidlyEncode(context + " " + id + " " + this.challenge));
                        return SaslResult.CONTINUE;
                    }
                }
                return SaslResult.ERROR;
            }
            case 2: {
                String[] response = this.stupidlyDecode(_c.getData()).split(" ");
                if (response.length < 2) {
                    return SaslResult.ERROR;
                }
                String cchal = response[0];
                String hash = response[1];
                String prehash = this.challenge + ":" + cchal + ":" + this.cookie;
                byte[] buf = md.digest(prehash.getBytes());
                String posthash = this.stupidlyEncode(buf);
                this.logger.debug("Authenticating Hash; data={} remote-hash={} local-hash={}", prehash, hash, posthash);
                if (0 == COL.compare(posthash, hash)) {
                    return SaslResult.OK;
                }
                return SaslResult.ERROR;
            }
        }
        return SaslResult.ERROR;
    }

    public String[] convertAuthTypes(int _types) {
        String[] stringArray;
        switch (_types) {
            case 1: {
                String[] stringArray2 = new String[1];
                stringArray = stringArray2;
                stringArray2[0] = AUTH_TYPE_EXTERNAL;
                break;
            }
            case 2: {
                String[] stringArray3 = new String[1];
                stringArray = stringArray3;
                stringArray3[0] = AUTH_TYPE_DBUS_COOKIE_SHA1;
                break;
            }
            case 4: {
                String[] stringArray4 = new String[1];
                stringArray = stringArray4;
                stringArray4[0] = AUTH_TYPE_ANONYMOUS;
                break;
            }
            case 3: {
                String[] stringArray5 = new String[2];
                stringArray5[0] = AUTH_TYPE_EXTERNAL;
                stringArray = stringArray5;
                stringArray5[1] = AUTH_TYPE_DBUS_COOKIE_SHA1;
                break;
            }
            case 6: {
                String[] stringArray6 = new String[2];
                stringArray6[0] = AUTH_TYPE_ANONYMOUS;
                stringArray = stringArray6;
                stringArray6[1] = AUTH_TYPE_DBUS_COOKIE_SHA1;
                break;
            }
            case 5: {
                String[] stringArray7 = new String[2];
                stringArray7[0] = AUTH_TYPE_ANONYMOUS;
                stringArray = stringArray7;
                stringArray7[1] = AUTH_TYPE_EXTERNAL;
                break;
            }
            case 7: {
                String[] stringArray8 = new String[3];
                stringArray8[0] = AUTH_TYPE_ANONYMOUS;
                stringArray8[1] = AUTH_TYPE_EXTERNAL;
                stringArray = stringArray8;
                stringArray8[2] = AUTH_TYPE_DBUS_COOKIE_SHA1;
                break;
            }
            default: {
                stringArray = new String[]{};
            }
        }
        return stringArray;
    }

    public boolean auth(SocketChannel _sock, AbstractTransport _transport) throws IOException {
        String luid = null;
        String kernelUid = null;
        long uid = this.saslConfig.getSaslUid().orElse(this.getUserId());
        luid = this.stupidlyEncode("" + uid);
        int failed = 0;
        int current = 0;
        SaslAuthState state = SaslAuthState.INITIAL_STATE;
        block57: while (state != SaslAuthState.FINISHED && state != SaslAuthState.FAILED) {
            this.logger.trace("Mode: {} AUTH state: {}", (Object)this.saslConfig.getMode(), (Object)state);
            switch (this.saslConfig.getMode().ordinal()) {
                case 1: {
                    Command c;
                    switch (state.ordinal()) {
                        case 0: {
                            _sock.write(ByteBuffer.wrap(new byte[]{0}));
                            this.send(_sock, SaslCommand.AUTH, new String[0]);
                            state = SaslAuthState.WAIT_DATA;
                            continue block57;
                        }
                        case 1: {
                            c = this.receive(_sock);
                            switch (c.getCommand().ordinal()) {
                                case 1: {
                                    switch (this.doChallenge(current, c).ordinal()) {
                                        case 1: {
                                            this.send(_sock, SaslCommand.DATA, c.getResponse());
                                            continue block57;
                                        }
                                        case 0: {
                                            this.send(_sock, SaslCommand.DATA, c.getResponse());
                                            state = SaslAuthState.WAIT_OK;
                                            continue block57;
                                        }
                                    }
                                    this.send(_sock, SaslCommand.ERROR, c.getResponse());
                                    continue block57;
                                }
                                case 2: {
                                    int available = c.getMechs() & ~(failed |= current);
                                    int retVal = this.handleReject(available, luid, _sock);
                                    if (retVal == -1) {
                                        state = SaslAuthState.FAILED;
                                        continue block57;
                                    }
                                    current = retVal;
                                    continue block57;
                                }
                                case 6: {
                                    if (state == SaslAuthState.NEGOTIATE_UNIX_FD) {
                                        state = SaslAuthState.FINISHED;
                                        this.logger.trace("File descriptors NOT supported by server");
                                        this.fileDescriptorSupported = false;
                                        this.send(_sock, SaslCommand.BEGIN, new String[0]);
                                        continue block57;
                                    }
                                    this.send(_sock, SaslCommand.CANCEL, new String[0]);
                                    state = SaslAuthState.WAIT_REJECT;
                                    continue block57;
                                }
                                case 3: {
                                    this.logger.trace("Authenticated");
                                    if (this.saslConfig.isFileDescriptorSupport()) {
                                        state = SaslAuthState.WAIT_DATA;
                                        this.logger.trace("Asking for file descriptor support");
                                        this.send(_sock, SaslCommand.NEGOTIATE_UNIX_FD, new String[0]);
                                        continue block57;
                                    }
                                    state = SaslAuthState.FINISHED;
                                    this.send(_sock, SaslCommand.BEGIN, new String[0]);
                                    continue block57;
                                }
                                case 8: {
                                    if (!this.saslConfig.isFileDescriptorSupport()) continue block57;
                                    state = SaslAuthState.FINISHED;
                                    this.logger.trace("File descriptors supported by server");
                                    this.fileDescriptorSupported = true;
                                    this.send(_sock, SaslCommand.BEGIN, new String[0]);
                                    continue block57;
                                }
                            }
                            this.send(_sock, SaslCommand.ERROR, INVALID_CMD_ERR);
                            continue block57;
                        }
                        case 2: {
                            c = this.receive(_sock);
                            switch (c.getCommand().ordinal()) {
                                case 3: {
                                    this.send(_sock, SaslCommand.BEGIN, new String[0]);
                                    state = SaslAuthState.FINISHED;
                                    continue block57;
                                }
                                case 1: 
                                case 6: {
                                    this.send(_sock, SaslCommand.CANCEL, new String[0]);
                                    state = SaslAuthState.WAIT_REJECT;
                                    continue block57;
                                }
                                case 2: {
                                    int available = c.getMechs() & ~(failed |= current);
                                    state = SaslAuthState.WAIT_DATA;
                                    if (0 != (available & 1)) {
                                        this.send(_sock, SaslCommand.AUTH, AUTH_TYPE_EXTERNAL, luid);
                                        current = 1;
                                        continue block57;
                                    }
                                    if (0 != (available & 2)) {
                                        this.send(_sock, SaslCommand.AUTH, AUTH_TYPE_DBUS_COOKIE_SHA1, luid);
                                        current = 2;
                                        continue block57;
                                    }
                                    if (0 != (available & 4)) {
                                        this.send(_sock, SaslCommand.AUTH, AUTH_TYPE_ANONYMOUS);
                                        current = 4;
                                        continue block57;
                                    }
                                    state = SaslAuthState.FAILED;
                                    continue block57;
                                }
                            }
                            this.send(_sock, SaslCommand.ERROR, INVALID_CMD_ERR);
                            continue block57;
                        }
                        case 3: {
                            c = this.receive(_sock);
                            if (c.getCommand() == SaslCommand.REJECTED) {
                                int available = c.getMechs() & ~(failed |= current);
                                int retVal = this.handleReject(available, luid, _sock);
                                if (retVal == -1) {
                                    state = SaslAuthState.FAILED;
                                    continue block57;
                                }
                                current = retVal;
                                continue block57;
                            }
                            state = SaslAuthState.FAILED;
                            continue block57;
                        }
                    }
                    state = SaslAuthState.FAILED;
                    continue block57;
                }
                case 0: {
                    Command c;
                    switch (state.ordinal()) {
                        case 0: {
                            ByteBuffer buf = ByteBuffer.allocate(1);
                            if (_sock instanceof NetworkChannel) {
                                _sock.read(buf);
                                state = SaslAuthState.WAIT_AUTH;
                                continue block57;
                            }
                            try {
                                int kuid = -1;
                                if (_transport instanceof AbstractUnixTransport) {
                                    AbstractUnixTransport aut = (AbstractUnixTransport)_transport;
                                    kuid = aut.getUid(_sock);
                                }
                                if (kuid >= 0) {
                                    kernelUid = this.stupidlyEncode("" + kuid);
                                }
                                state = SaslAuthState.WAIT_AUTH;
                            }
                            catch (SocketException _ex) {
                                state = SaslAuthState.FAILED;
                            }
                            continue block57;
                        }
                        case 4: {
                            c = this.receive(_sock);
                            switch (c.getCommand().ordinal()) {
                                case 0: {
                                    switch (this.doResponse(current, luid, kernelUid, c).ordinal()) {
                                        case 1: {
                                            this.send(_sock, SaslCommand.DATA, c.getResponse());
                                            current = c.getMechs();
                                            state = SaslAuthState.WAIT_DATA;
                                            continue block57;
                                        }
                                        case 0: {
                                            this.send(_sock, SaslCommand.OK, this.saslConfig.getGuid());
                                            state = SaslAuthState.WAIT_BEGIN;
                                            current = 0;
                                            continue block57;
                                        }
                                    }
                                    this.send(_sock, SaslCommand.REJECTED, this.convertAuthTypes(this.saslConfig.getAuthMode()));
                                    current = 0;
                                    continue block57;
                                }
                                case 6: {
                                    this.send(_sock, SaslCommand.REJECTED, this.convertAuthTypes(this.saslConfig.getAuthMode()));
                                    continue block57;
                                }
                                case 4: {
                                    state = SaslAuthState.FAILED;
                                    continue block57;
                                }
                            }
                            this.send(_sock, SaslCommand.ERROR, INVALID_CMD_ERR);
                            continue block57;
                        }
                        case 1: {
                            c = this.receive(_sock);
                            switch (c.getCommand().ordinal()) {
                                case 1: {
                                    switch (this.doResponse(current, luid, kernelUid, c).ordinal()) {
                                        case 1: {
                                            this.send(_sock, SaslCommand.DATA, c.getResponse());
                                            state = SaslAuthState.WAIT_DATA;
                                            continue block57;
                                        }
                                        case 0: {
                                            this.send(_sock, SaslCommand.OK, this.saslConfig.getGuid());
                                            state = SaslAuthState.WAIT_BEGIN;
                                            current = 0;
                                            continue block57;
                                        }
                                    }
                                    this.send(_sock, SaslCommand.REJECTED, this.convertAuthTypes(this.saslConfig.getAuthMode()));
                                    current = 0;
                                    continue block57;
                                }
                                case 5: 
                                case 6: {
                                    this.send(_sock, SaslCommand.REJECTED, this.convertAuthTypes(this.saslConfig.getAuthMode()));
                                    state = SaslAuthState.WAIT_AUTH;
                                    continue block57;
                                }
                                case 4: {
                                    state = SaslAuthState.FAILED;
                                    continue block57;
                                }
                            }
                            this.send(_sock, SaslCommand.ERROR, INVALID_CMD_ERR);
                            continue block57;
                        }
                        case 5: {
                            c = this.receive(_sock);
                            switch (c.getCommand().ordinal()) {
                                case 5: 
                                case 6: {
                                    this.send(_sock, SaslCommand.REJECTED, this.convertAuthTypes(this.saslConfig.getAuthMode()));
                                    state = SaslAuthState.WAIT_AUTH;
                                    continue block57;
                                }
                                case 4: {
                                    state = SaslAuthState.FINISHED;
                                    continue block57;
                                }
                                case 7: {
                                    this.logger.debug("File descriptor negotiation requested");
                                    if (!this.saslConfig.isFileDescriptorSupport()) {
                                        this.send(_sock, SaslCommand.ERROR, new String[0]);
                                        continue block57;
                                    }
                                    this.send(_sock, SaslCommand.AGREE_UNIX_FD, new String[0]);
                                    continue block57;
                                }
                            }
                            this.send(_sock, SaslCommand.ERROR, INVALID_CMD_ERR);
                            continue block57;
                        }
                    }
                    state = SaslAuthState.FAILED;
                    continue block57;
                }
            }
            return false;
        }
        return state == SaslAuthState.FINISHED;
    }

    public boolean isFileDescriptorSupported() {
        return this.fileDescriptorSupported;
    }

    private int handleReject(int _available, String _luid, SocketChannel _sock) throws IOException {
        int current = -1;
        if (0 != (_available & 1)) {
            this.send(_sock, SaslCommand.AUTH, AUTH_TYPE_EXTERNAL, _luid);
            current = 1;
        } else if (0 != (_available & 2)) {
            this.send(_sock, SaslCommand.AUTH, AUTH_TYPE_DBUS_COOKIE_SHA1, _luid);
            current = 2;
        } else if (0 != (_available & 4)) {
            this.send(_sock, SaslCommand.AUTH, AUTH_TYPE_ANONYMOUS);
            current = 4;
        }
        return current;
    }

    private long getUserId() {
        if (!Util.isWindows()) {
            return new UnixSystem().getUid();
        }
        return 0L;
    }

    static {
        COL.setDecomposition(2);
        COL.setStrength(0);
        SYSPROP_USER_HOME = System.getProperty("user.home");
        DBUS_TEST_HOME_DIR = System.getProperty("DBUS_TEST_HOMEDIR");
        DBUS_KEYRINGS_DIR = new File(SYSPROP_USER_HOME, ".dbus-keyrings");
        BAD_FILE_PERMISSIONS = Set.of(PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE, PosixFilePermission.OTHERS_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE);
    }

    public static class Command {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private SaslCommand command;
        private int mechs;
        private String data;
        private String response;

        public Command() {
        }

        public Command(String _s) throws IOException {
            String[] ss = _s.split(" ");
            LoggingHelper.logIf(this.logger.isTraceEnabled(), () -> this.logger.trace("Creating command from: {}", (Object)Arrays.toString(ss)));
            if (0 == COL.compare(ss[0], "OK")) {
                this.command = SaslCommand.OK;
                this.data = ss[1];
            } else if (0 == COL.compare(ss[0], "AUTH")) {
                this.command = SaslCommand.AUTH;
                if (ss.length > 1) {
                    if (0 == COL.compare(ss[1], SASL.AUTH_TYPE_EXTERNAL)) {
                        this.mechs = 1;
                    } else if (0 == COL.compare(ss[1], SASL.AUTH_TYPE_DBUS_COOKIE_SHA1)) {
                        this.mechs = 2;
                    } else if (0 == COL.compare(ss[1], SASL.AUTH_TYPE_ANONYMOUS)) {
                        this.mechs = 4;
                    }
                }
                if (ss.length > 2) {
                    this.data = ss[2];
                }
            } else if (0 == COL.compare(ss[0], "DATA")) {
                this.command = SaslCommand.DATA;
                this.data = ss.length < 2 ? null : ss[1];
            } else if (0 == COL.compare(ss[0], "REJECTED")) {
                this.command = SaslCommand.REJECTED;
                for (int i = 1; i < ss.length; ++i) {
                    if (0 == COL.compare(ss[i], SASL.AUTH_TYPE_EXTERNAL)) {
                        this.mechs |= 1;
                        continue;
                    }
                    if (0 == COL.compare(ss[i], SASL.AUTH_TYPE_DBUS_COOKIE_SHA1)) {
                        this.mechs |= 2;
                        continue;
                    }
                    if (0 != COL.compare(ss[i], SASL.AUTH_TYPE_ANONYMOUS)) continue;
                    this.mechs |= 4;
                }
            } else if (0 == COL.compare(ss[0], "BEGIN")) {
                this.command = SaslCommand.BEGIN;
            } else if (0 == COL.compare(ss[0], "CANCEL")) {
                this.command = SaslCommand.CANCEL;
            } else if (0 == COL.compare(ss[0], "ERROR")) {
                this.command = SaslCommand.ERROR;
                this.data = ss[1];
            } else if (0 == COL.compare(ss[0], "NEGOTIATE_UNIX_FD")) {
                this.command = SaslCommand.NEGOTIATE_UNIX_FD;
            } else if (0 == COL.compare(ss[0], "AGREE_UNIX_FD")) {
                this.command = SaslCommand.AGREE_UNIX_FD;
            } else {
                throw new IOException("Invalid Command " + ss[0]);
            }
            this.logger.trace("Created command: {}", (Object)this);
        }

        public SaslCommand getCommand() {
            return this.command;
        }

        public int getMechs() {
            return this.mechs;
        }

        public String getData() {
            return this.data;
        }

        public String getResponse() {
            return this.response;
        }

        public void setResponse(String _s) {
            this.response = _s;
        }

        public String toString() {
            return "Command(" + String.valueOf((Object)this.command) + ", " + this.mechs + ", " + this.data + ")";
        }
    }

    public static enum SaslCommand {
        AUTH,
        DATA,
        REJECTED,
        OK,
        BEGIN,
        CANCEL,
        ERROR,
        NEGOTIATE_UNIX_FD,
        AGREE_UNIX_FD;

    }

    public static enum SaslResult {
        OK,
        CONTINUE,
        ERROR,
        REJECT;

    }

    static enum SaslAuthState {
        INITIAL_STATE,
        WAIT_DATA,
        WAIT_OK,
        WAIT_REJECT,
        WAIT_AUTH,
        WAIT_BEGIN,
        NEGOTIATE_UNIX_FD,
        FINISHED,
        FAILED;

    }

    public static enum SaslMode {
        SERVER,
        CLIENT;

    }
}

