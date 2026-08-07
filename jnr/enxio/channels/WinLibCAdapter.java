
package jnr.enxio.channels;

import java.nio.ByteBuffer;
import jnr.enxio.channels.Native;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.IgnoreError;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.provider.LoadedLibrary;
import jnr.ffi.types.size_t;
import jnr.ffi.types.ssize_t;

public final class WinLibCAdapter
implements Native.LibC,
LoadedLibrary {
    private LibMSVCRT win;

    public WinLibCAdapter(LibMSVCRT winlibc) {
        this.win = winlibc;
    }

    @Override
    public int close(int fd) {
        return this.win._close(fd);
    }

    @Override
    public int read(int fd, ByteBuffer data, long size) {
        return this.win._read(fd, data, size);
    }

    @Override
    public int read(int fd, byte[] data, long size) {
        return this.win._read(fd, data, size);
    }

    @Override
    public int write(int fd, ByteBuffer data, long size) {
        return this.win._write(fd, data, size);
    }

    @Override
    public int write(int fd, byte[] data, long size) {
        return this.win._write(fd, data, size);
    }

    @Override
    public int pipe(int[] fds) {
        return this.win._pipe(fds);
    }

    @Override
    public String strerror(int error) {
        return this.win._strerror(error);
    }

    @Override
    public Runtime getRuntime() {
        return Runtime.getRuntime(this.win);
    }

    @Override
    public int fcntl(int fd, int cmd, int data) {
        throw new UnsupportedOperationException("fcntl isn't supported on Windows");
    }

    @Override
    public int poll(ByteBuffer pfds, int nfds, int timeout) {
        throw new UnsupportedOperationException("poll isn't supported on Windows");
    }

    @Override
    public int poll(Pointer pfds, int nfds, int timeout) {
        throw new UnsupportedOperationException("poll isn't supported on Windows");
    }

    @Override
    public int kqueue() {
        throw new UnsupportedOperationException("kqueue isn't supported on Windows");
    }

    @Override
    public int kevent(int kq, ByteBuffer changebuf, int nchanges, ByteBuffer eventbuf, int nevents, Native.Timespec timeout) {
        throw new UnsupportedOperationException("kevent isn't supported on Windows");
    }

    @Override
    public int kevent(int kq, Pointer changebuf, int nchanges, Pointer eventbuf, int nevents, Native.Timespec timeout) {
        throw new UnsupportedOperationException("kevent isn't supported on Windows");
    }

    @Override
    public int shutdown(int s, int how) {
        throw new UnsupportedOperationException("shutdown isn't supported on Windows");
    }

    public static interface LibMSVCRT {
        public int _close(int var1);

        @ssize_t
        public int _read(int var1, @Out ByteBuffer var2, @size_t long var3);

        @ssize_t
        public int _read(int var1, @Out byte[] var2, @size_t long var3);

        @ssize_t
        public int _write(int var1, @In ByteBuffer var2, @size_t long var3);

        @ssize_t
        public int _write(int var1, @In byte[] var2, @size_t long var3);

        public int _pipe(@Out int[] var1);

        @IgnoreError
        public String _strerror(int var1);
    }
}

