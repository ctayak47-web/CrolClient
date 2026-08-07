
package jnr.unixsocket;

import java.io.File;
import java.io.IOException;
import java.net.SocketOption;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.SocketLevel;
import jnr.ffi.Platform;
import jnr.ffi.byref.IntByReference;
import jnr.unixsocket.Credentials;
import jnr.unixsocket.Native;
import jnr.unixsocket.SockAddrUnix;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketOptions;

final class Common {
    private static Platform.OS currentOS = Platform.getNativePlatform().getOS();
    private static final Map<SocketOption<?>, jnr.constants.platform.SocketOption> wMap = new HashMap();
    private static final Map<SocketOption<?>, jnr.constants.platform.SocketOption> rMap = new HashMap();

    private Common() {
    }

    static UnixSocketAddress bind(int fd, UnixSocketAddress local) throws IOException {
        SockAddrUnix sa;
        if (null == local) {
            sa = SockAddrUnix.create();
            sa.setFamily(ProtocolFamily.PF_UNIX);
            if (currentOS == Platform.OS.LINUX) {
                sa.setPath("");
            } else {
                File f = Files.createTempFile("jnr-unixsocket-tmp", ".sock", new FileAttribute[0]).toFile();
                f.deleteOnExit();
                f.delete();
                sa.setPath(f.getPath());
            }
        } else {
            sa = local.getStruct();
        }
        if (Native.bind(fd, sa, sa.length()) < 0) {
            throw new IOException(Native.getLastErrorString());
        }
        return Common.getsockname(fd);
    }

    static UnixSocketAddress getsockname(int sockfd) {
        UnixSocketAddress local = new UnixSocketAddress();
        SockAddrUnix addr = local.getStruct();
        IntByReference len = new IntByReference(addr.getMaximumLength());
        if (Native.libc().getsockname(sockfd, addr, len) < 0) {
            throw new Error(Native.getLastErrorString());
        }
        addr.updatePath((Integer)len.getValue());
        return local;
    }

    static UnixSocketAddress getpeername(int sockfd) {
        UnixSocketAddress remote = new UnixSocketAddress();
        SockAddrUnix addr = remote.getStruct();
        IntByReference len = new IntByReference(addr.getMaximumLength());
        if (Native.libc().getpeername(sockfd, addr, len) < 0) {
            throw new Error(Native.getLastErrorString());
        }
        addr.updatePath((Integer)len.getValue());
        return remote;
    }

    static <T> T getSocketOption(int fd, SocketOption<?> name) throws IOException {
        jnr.constants.platform.SocketOption optname = rMap.get(name);
        if (null == optname) {
            throw new AssertionError((Object)"Option not found");
        }
        Class<?> type = name.type();
        if (type == Credentials.class) {
            return (T)Credentials.getCredentials(fd);
        }
        if (type == Integer.class) {
            return (T)Integer.valueOf(Native.getsockopt(fd, SocketLevel.SOL_SOCKET, optname.intValue()));
        }
        return (T)Boolean.valueOf(Native.getboolsockopt(fd, SocketLevel.SOL_SOCKET, optname.intValue()));
    }

    static void setSocketOption(int fd, SocketOption<?> name, Object value) throws IOException {
        int i;
        int optvalue;
        if (null == value) {
            throw new IllegalArgumentException("Invalid option value");
        }
        jnr.constants.platform.SocketOption optname = wMap.get(name);
        if (null == optname) {
            throw new AssertionError((Object)"Option not found or not writable");
        }
        Class<?> type = name.type();
        if (type != Integer.class && type != Boolean.class) {
            throw new AssertionError((Object)"Unsupported option type");
        }
        if (type == Integer.class) {
            optvalue = (Integer)value;
        } else {
            int n = optvalue = (Boolean)value != false ? 1 : 0;
        }
        if ((name == UnixSocketOptions.SO_RCVBUF || name == UnixSocketOptions.SO_SNDBUF) && (i = ((Integer)value).intValue()) < 0) {
            throw new IllegalArgumentException("Invalid send/receive buffer size");
        }
        if ((name == UnixSocketOptions.SO_RCVTIMEO || name == UnixSocketOptions.SO_SNDTIMEO) && (i = ((Integer)value).intValue()) < 0) {
            throw new IllegalArgumentException("Invalid send/receive timeout");
        }
        if (0 != Native.setsockopt(fd, SocketLevel.SOL_SOCKET, optname, optvalue)) {
            throw new IOException(Native.getLastErrorString());
        }
    }

    static {
        wMap.put(UnixSocketOptions.SO_RCVBUF, jnr.constants.platform.SocketOption.SO_RCVBUF);
        wMap.put(UnixSocketOptions.SO_SNDBUF, jnr.constants.platform.SocketOption.SO_SNDBUF);
        wMap.put(UnixSocketOptions.SO_RCVTIMEO, jnr.constants.platform.SocketOption.SO_RCVTIMEO);
        wMap.put(UnixSocketOptions.SO_SNDTIMEO, jnr.constants.platform.SocketOption.SO_SNDTIMEO);
        wMap.put(UnixSocketOptions.SO_KEEPALIVE, jnr.constants.platform.SocketOption.SO_KEEPALIVE);
        wMap.put(UnixSocketOptions.SO_PASSCRED, jnr.constants.platform.SocketOption.SO_PASSCRED);
        rMap.putAll(wMap);
        rMap.put(UnixSocketOptions.SO_PEERCRED, jnr.constants.platform.SocketOption.SO_PEERCRED);
    }
}

