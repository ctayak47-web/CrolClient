
package jnr.unixsocket.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import jnr.constants.platform.Errno;
import jnr.enxio.channels.Native;
import jnr.enxio.channels.NativeException;

final class Common {
    private int _fd = -1;

    Common(int fd) {
        this._fd = fd;
    }

    void setFD(int fd) {
        this._fd = fd;
    }

    int getFD() {
        return this._fd;
    }

    int read(ByteBuffer dst) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(dst.remaining());
        int n = Native.read(this._fd, buffer);
        buffer.flip();
        dst.put(buffer);
        switch (n) {
            case 0: {
                return -1;
            }
            case -1: {
                Errno lastError = Native.getLastError();
                switch (lastError) {
                    case EAGAIN: 
                    case EWOULDBLOCK: {
                        return 0;
                    }
                }
                throw new NativeException(Native.getLastErrorString(), lastError);
            }
        }
        return n;
    }

    long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        long total = 0L;
        for (int i = 0; i < length; ++i) {
            ByteBuffer dst = dsts[offset + i];
            long read = this.read(dst);
            if (read == -1L) {
                return read;
            }
            total += read;
        }
        return total;
    }

    int write(ByteBuffer src) throws IOException {
        int r = src.remaining();
        ByteBuffer buffer = ByteBuffer.allocate(r);
        buffer.put(src);
        buffer.position(0);
        int n = Native.write(this._fd, buffer);
        if (n >= 0) {
            if (n < r) {
                src.position(src.position() - (r - n));
            }
        } else {
            Errno lastError = Native.getLastError();
            switch (lastError) {
                case EAGAIN: 
                case EWOULDBLOCK: {
                    src.position(src.position() - r);
                    return 0;
                }
            }
            throw new NativeException(Native.getLastErrorString(), lastError);
        }
        return n;
    }

    long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        long result = 0L;
        for (int index = offset; index < length; ++index) {
            int w;
            ByteBuffer buffer = srcs[index];
            int remaining = buffer.remaining();
            int written = 0;
            while ((w = this.write(buffer)) != 0 && (written += w) != remaining) {
            }
            result += (long)written;
            if (written < remaining) break;
        }
        return result;
    }
}

