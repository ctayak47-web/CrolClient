
package jnr.unixsocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import jnr.constants.platform.Errno;
import jnr.constants.platform.Fcntl;
import jnr.constants.platform.OpenFlags;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import jnr.constants.platform.SocketLevel;
import jnr.constants.platform.SocketOption;
import jnr.ffi.LastError;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Platform;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Transient;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.types.size_t;
import jnr.ffi.types.ssize_t;
import jnr.posix.DefaultNativeTimeval;
import jnr.posix.Timeval;
import jnr.unixsocket.SockAddrUnix;

class Native {
    static final String[] libnames;
    static final LibC INSTANCE;

    Native() {
    }

    static final LibC libsocket() {
        return INSTANCE;
    }

    static final LibC libc() {
        return INSTANCE;
    }

    static int socket(ProtocolFamily domain, Sock type, int protocol) throws IOException {
        int fd = Native.libsocket().socket(domain.intValue(), type.intValue(), protocol);
        if (fd < 0) {
            throw new IOException(Native.getLastErrorString());
        }
        return fd;
    }

    static int socketpair(ProtocolFamily domain, Sock type, int protocol, int[] sv) throws IOException {
        if (Native.libsocket().socketpair(domain.intValue(), type.intValue(), protocol, sv) < 0) {
            throw new IOException("socketpair(2) failed " + Native.getLastErrorString());
        }
        return 0;
    }

    static int listen(int fd, int backlog) {
        return Native.libsocket().listen(fd, backlog);
    }

    static int bind(int fd, SockAddrUnix addr, int len) {
        return Native.libsocket().bind(fd, addr, len);
    }

    static int accept(int fd, SockAddrUnix addr, IntByReference len) {
        return Native.libsocket().accept(fd, addr, len);
    }

    static int connect(int fd, SockAddrUnix addr, int len) {
        return Native.libsocket().connect(fd, addr, len);
    }

    static String getLastErrorString() {
        return Native.strerror(LastError.getLastError(Runtime.getSystemRuntime()));
    }

    static Errno getLastError() {
        return Errno.valueOf(LastError.getLastError(Runtime.getSystemRuntime()));
    }

    static String strerror(int error) {
        return Native.libc().strerror(error);
    }

    public static void setBlocking(int fd, boolean block) {
        int flags = Native.libc().fcntl(fd, LibC.F_GETFL, 0);
        flags = block ? (flags &= ~LibC.O_NONBLOCK) : (flags |= LibC.O_NONBLOCK);
        Native.libc().fcntl(fd, LibC.F_SETFL, flags);
    }

    public static int setsockopt(int s, SocketLevel level, SocketOption optname, boolean optval) {
        return Native.setsockopt(s, level, optname, optval ? 1 : 0);
    }

    public static int setsockopt(int s, SocketLevel level, SocketOption optname, int optval) {
        if (optname == SocketOption.SO_RCVTIMEO || optname == SocketOption.SO_SNDTIMEO) {
            DefaultNativeTimeval t = new DefaultNativeTimeval(Runtime.getSystemRuntime());
            t.setTime(new long[]{optval / 1000, (long)optval % 1000L * 1000L});
            return Native.libsocket().setsockopt(s, level.intValue(), optname.intValue(), t, DefaultNativeTimeval.size(t));
        }
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.nativeOrder());
        buf.putInt(optval).flip();
        return Native.libsocket().setsockopt(s, level.intValue(), optname.intValue(), buf, buf.remaining());
    }

    public static int getsockopt(int s, SocketLevel level, int optname) {
        if (optname == SocketOption.SO_RCVTIMEO.intValue() || optname == SocketOption.SO_SNDTIMEO.intValue()) {
            DefaultNativeTimeval t = new DefaultNativeTimeval(Runtime.getSystemRuntime());
            IntByReference ref = new IntByReference(DefaultNativeTimeval.size(t));
            Native.libsocket().getsockopt(s, level.intValue(), optname, t, ref);
            return t.tv_sec.intValue() * 1000 + t.tv_usec.intValue() / 1000;
        }
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.nativeOrder());
        IntByReference ref = new IntByReference(4);
        Native.libsocket().getsockopt(s, level.intValue(), optname, buf, ref);
        return buf.getInt();
    }

    public static int getsockopt(int s, SocketLevel level, SocketOption optname, Struct data) {
        Pointer struct_ptr = Struct.getMemory(data);
        IntByReference ref = new IntByReference(Struct.size(data));
        ByteBuffer buf = ByteBuffer.wrap((byte[])struct_ptr.array());
        return Native.libsocket().getsockopt(s, level.intValue(), optname.intValue(), buf, ref);
    }

    public static boolean getboolsockopt(int s, SocketLevel level, int optname) {
        return Native.getsockopt(s, level, optname) != 0;
    }

    public static int sendto(int fd, ByteBuffer src, SockAddrUnix addr, int len) throws IOException {
        int n;
        if (src == null) {
            throw new IllegalArgumentException("Source buffer cannot be null");
        }
        while ((n = Native.libsocket().sendto(fd, src, src.remaining(), 0, addr, len)) < 0 && Errno.EINTR.equals(Native.getLastError())) {
        }
        if (n > 0) {
            src.position(src.position() + n);
        }
        return n;
    }

    public static int recvfrom(int fd, ByteBuffer dst, SockAddrUnix addr) throws IOException {
        int n;
        IntByReference addrlen;
        if (dst == null) {
            throw new IllegalArgumentException("Destination buffer cannot be null");
        }
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        }
        IntByReference intByReference = addrlen = null == addr ? null : new IntByReference(addr.getMaximumLength());
        while ((n = Native.libsocket().recvfrom(fd, dst, dst.remaining(), 0, addr, addrlen)) < 0 && Errno.EINTR.equals(Native.getLastError())) {
        }
        if (n > 0) {
            dst.position(dst.position() + n);
        }
        return n;
    }

    static {
        String[] stringArray;
        if (Platform.getNativePlatform().getOS() == Platform.OS.SOLARIS) {
            String[] stringArray2 = new String[3];
            stringArray2[0] = "socket";
            stringArray2[1] = "nsl";
            stringArray = stringArray2;
            stringArray2[2] = Platform.getNativePlatform().getStandardCLibraryName();
        } else {
            String[] stringArray3 = new String[1];
            stringArray = stringArray3;
            stringArray3[0] = Platform.getNativePlatform().getStandardCLibraryName();
        }
        libnames = stringArray;
        LibraryLoader<LibC> loader = LibraryLoader.create(LibC.class);
        for (String libraryName : libnames) {
            loader.library(libraryName);
        }
        INSTANCE = loader.load();
    }

    public static interface LibC {
        public static final int F_GETFL = Fcntl.F_GETFL.intValue();
        public static final int F_SETFL = Fcntl.F_SETFL.intValue();
        public static final int O_NONBLOCK = OpenFlags.O_NONBLOCK.intValue();

        public int socket(int var1, int var2, int var3);

        public int listen(int var1, int var2);

        public int bind(int var1, @In @Out @Transient SockAddrUnix var2, int var3);

        public int accept(int var1, @Out SockAddrUnix var2, @In @Out IntByReference var3);

        public int connect(int var1, @In @Transient SockAddrUnix var2, int var3);

        public int getsockname(int var1, @Out SockAddrUnix var2, @In @Out IntByReference var3);

        public int getpeername(int var1, @Out SockAddrUnix var2, @In @Out IntByReference var3);

        public int socketpair(int var1, int var2, int var3, @Out int[] var4);

        public int fcntl(int var1, int var2, int var3);

        public int getsockopt(int var1, int var2, int var3, @Out ByteBuffer var4, @In @Out IntByReference var5);

        public int getsockopt(int var1, int var2, int var3, @Out Timeval var4, @In @Out IntByReference var5);

        public int setsockopt(int var1, int var2, int var3, @In ByteBuffer var4, int var5);

        public int setsockopt(int var1, int var2, int var3, @In Timeval var4, int var5);

        public String strerror(int var1);

        @ssize_t
        public int sendto(int var1, @In ByteBuffer var2, @size_t long var3, int var5, @In @Transient SockAddrUnix var6, int var7);

        @ssize_t
        public int recvfrom(int var1, @Out ByteBuffer var2, @size_t long var3, int var5, @Out SockAddrUnix var6, @In @Out IntByReference var7);
    }
}

