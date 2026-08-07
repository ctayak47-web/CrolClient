
package jnr.enxio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import jnr.constants.platform.Errno;
import jnr.constants.platform.Fcntl;
import jnr.constants.platform.OpenFlags;
import jnr.enxio.channels.NativeException;
import jnr.enxio.channels.WinLibCAdapter;
import jnr.ffi.LastError;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Platform;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.annotations.IgnoreError;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Transient;
import jnr.ffi.types.size_t;
import jnr.ffi.types.ssize_t;

public final class Native {
    static LibC libc() {
        return SingletonHolder.libc;
    }

    static Runtime getRuntime() {
        return SingletonHolder.runtime;
    }

    public static int close(int fd) throws IOException {
        int rc;
        while ((rc = Native.libc().close(fd)) < 0 && Errno.EINTR.equals(Native.getLastError())) {
        }
        if (rc < 0) {
            String message = String.format("Error closing fd %d: %s", fd, Native.getLastErrorString());
            throw new NativeException(message, Native.getLastError());
        }
        return rc;
    }

    public static int read(int fd, ByteBuffer dst) throws IOException {
        int n;
        if (dst == null) {
            throw new NullPointerException("Destination buffer cannot be null");
        }
        if (dst.isReadOnly()) {
            throw new IllegalArgumentException("Read-only buffer");
        }
        while ((n = Native.libc().read(fd, dst, (long)dst.remaining())) < 0 && Errno.EINTR.equals(Native.getLastError())) {
        }
        if (n > 0) {
            dst.position(dst.position() + n);
        }
        return n;
    }

    public static int write(int fd, ByteBuffer src) throws IOException {
        int n;
        if (src == null) {
            throw new NullPointerException("Source buffer cannot be null");
        }
        while ((n = Native.libc().write(fd, src, (long)src.remaining())) < 0 && Errno.EINTR.equals(Native.getLastError())) {
        }
        if (n > 0) {
            src.position(src.position() + n);
        }
        return n;
    }

    public static void setBlocking(int fd, boolean block) {
        int flags = Native.libc().fcntl(fd, LibC.F_GETFL, 0);
        flags = block ? (flags &= ~LibC.O_NONBLOCK) : (flags |= LibC.O_NONBLOCK);
        Native.libc().fcntl(fd, LibC.F_SETFL, flags);
    }

    public static int shutdown(int fd, int how) {
        return Native.libc().shutdown(fd, how);
    }

    public static String getLastErrorString() {
        return Native.libc().strerror(LastError.getLastError(Native.getRuntime()));
    }

    public static Errno getLastError() {
        return Errno.valueOf(LastError.getLastError(Native.getRuntime()));
    }

    private static final class SingletonHolder {
        static final LibC libc;
        static final Runtime runtime;

        private SingletonHolder() {
        }

        static {
            Platform platform = Platform.getNativePlatform();
            LibraryLoader<LibC> loader = LibraryLoader.create(LibC.class);
            loader.library(platform.getStandardCLibraryName());
            if (platform.getOS() == Platform.OS.SOLARIS) {
                loader.library("socket");
            }
            LibC straight = loader.load();
            if (platform.getOS() == Platform.OS.WINDOWS) {
                WinLibCAdapter.LibMSVCRT mslib = LibraryLoader.create(WinLibCAdapter.LibMSVCRT.class).load(platform.getStandardCLibraryName());
                libc = new WinLibCAdapter(mslib);
            } else {
                libc = straight;
            }
            runtime = Runtime.getRuntime(libc);
        }
    }

    public static interface LibC {
        public static final int F_GETFL = Fcntl.F_GETFL.intValue();
        public static final int F_SETFL = Fcntl.F_SETFL.intValue();
        public static final int O_NONBLOCK = OpenFlags.O_NONBLOCK.intValue();

        public int close(int var1);

        @ssize_t
        public int read(int var1, @Out ByteBuffer var2, @size_t long var3);

        @ssize_t
        public int read(int var1, @Out byte[] var2, @size_t long var3);

        @ssize_t
        public int write(int var1, @In ByteBuffer var2, @size_t long var3);

        @ssize_t
        public int write(int var1, @In byte[] var2, @size_t long var3);

        public int fcntl(int var1, int var2, int var3);

        public int poll(@In @Out ByteBuffer var1, int var2, int var3);

        public int poll(@In @Out Pointer var1, int var2, int var3);

        public int kqueue();

        public int kevent(int var1, @In ByteBuffer var2, int var3, @Out ByteBuffer var4, int var5, @In @Transient Timespec var6);

        public int kevent(int var1, @In Pointer var2, int var3, @Out Pointer var4, int var5, @In @Transient Timespec var6);

        public int pipe(@Out int[] var1);

        public int shutdown(int var1, int var2);

        @IgnoreError
        public String strerror(int var1);
    }

    public static final class Timespec
    extends Struct {
        public final Struct.SignedLong tv_sec = new Struct.SignedLong(this);
        public final Struct.SignedLong tv_nsec = new Struct.SignedLong(this);

        public Timespec() {
            super(Native.getRuntime());
        }

        public Timespec(Runtime runtime) {
            super(runtime);
        }

        public Timespec(long sec, long nsec) {
            super(Native.getRuntime());
            this.tv_sec.set(sec);
            this.tv_nsec.set(nsec);
        }
    }
}

