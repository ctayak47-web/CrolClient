
package jnr.posix;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import jnr.ffi.Pointer;
import jnr.posix.POSIX;
import jnr.posix.UnixLibC;

public abstract class SpawnFileAction {
    abstract boolean act(POSIX var1, Pointer var2);

    public static SpawnFileAction dup(int fd, int newfd) {
        return new Dup(fd, newfd);
    }

    public static SpawnFileAction open(String path, int fd, int flags, int mode) {
        return new Open(path, fd, flags, mode);
    }

    public static SpawnFileAction close(int fd) {
        return new Close(fd);
    }

    private static final class Dup
    extends SpawnFileAction {
        final int fd;
        final int newfd;

        public Dup(int fd, int newfd) {
            this.fd = fd;
            this.newfd = newfd;
        }

        @Override
        final boolean act(POSIX posix, Pointer nativeFileActions) {
            return ((UnixLibC)posix.libc()).posix_spawn_file_actions_adddup2(nativeFileActions, this.fd, this.newfd) == 0;
        }

        public String toString() {
            return "SpawnFileAction::Dup(old = " + this.fd + ", new = " + this.newfd + ")";
        }
    }

    private static final class Open
    extends SpawnFileAction {
        final String path;
        final int fd;
        final int flags;
        final int mode;
        final ByteBuffer nativePath;

        public Open(String path, int fd, int flags, int mode) {
            this.path = path;
            this.fd = fd;
            this.flags = flags;
            this.mode = mode;
            this.nativePath = this.defensiveCopy(path);
        }

        private ByteBuffer defensiveCopy(String path) {
            CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
            int bpc = (int)encoder.maxBytesPerChar();
            int size = (path.length() + 1) * bpc;
            ByteBuffer nativePath = ByteBuffer.allocateDirect(size);
            encoder.encode(CharBuffer.wrap(path), nativePath, true);
            nativePath.flip();
            nativePath.limit(nativePath.limit() + bpc);
            return nativePath;
        }

        @Override
        final boolean act(POSIX posix, Pointer nativeFileActions) {
            return ((UnixLibC)posix.libc()).posix_spawn_file_actions_addopen(nativeFileActions, this.fd, this.nativePath, this.flags, this.mode) == 0;
        }

        public String toString() {
            return "SpawnFileAction::Open(path = '" + this.path + "', fd = " + this.fd + ", flags = " + Integer.toHexString(this.flags) + ", mode = " + Integer.toHexString(this.mode) + ")";
        }
    }

    private static final class Close
    extends SpawnFileAction {
        final int fd;

        public Close(int fd) {
            this.fd = fd;
        }

        @Override
        final boolean act(POSIX posix, Pointer nativeFileActions) {
            return ((UnixLibC)posix.libc()).posix_spawn_file_actions_addclose(nativeFileActions, this.fd) == 0;
        }

        public String toString() {
            return "SpawnFileAction::Close(fd = " + this.fd + ")";
        }
    }
}

