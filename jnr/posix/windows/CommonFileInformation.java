
package jnr.posix.windows;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public abstract class CommonFileInformation
extends Struct {
    public static int FILE_ATTRIBUTE_READONLY = 1;
    public static int FILE_ATTRIBUTE_DIRECTORY = 16;
    public static final int NANOSECONDS = 1000000000;
    private static final double DAYS_BETWEEN_WINDOWS_AND_UNIX = 134774.4825;
    private static final long NANOSECONDS_TO_UNIX_EPOCH_FROM_WINDOWS = -6802270473709551616L;

    protected CommonFileInformation(Runtime runtime) {
        super(runtime);
    }

    public abstract int getFileAttributes();

    public abstract HackyFileTime getCreationTime();

    public abstract HackyFileTime getLastAccessTime();

    public abstract HackyFileTime getLastWriteTime();

    public abstract long getFileSizeHigh();

    public abstract long getFileSizeLow();

    public int getMode(String path) {
        int attr = this.getFileAttributes();
        int mode = 256;
        if ((attr & FILE_ATTRIBUTE_READONLY) == 0) {
            mode |= 0x80;
        }
        if ((path = path.toLowerCase()) != null && ((mode |= (attr & FILE_ATTRIBUTE_DIRECTORY) != 0 ? 16448 : 32768) & 0x8000) != 0 && (path.endsWith(".bat") || path.endsWith(".cmd") || path.endsWith(".com") || path.endsWith(".exe"))) {
            mode |= 0x40;
        }
        mode |= (mode & 0x1C0) >> 3;
        mode |= (mode & 0x1C0) >> 6;
        return mode;
    }

    public long getLastWriteTimeNanoseconds() {
        return this.epochNanos(this.getLastWriteTime().getLongValue());
    }

    public long getLastAccessTimeNanoseconds() {
        return this.epochNanos(this.getLastAccessTime().getLongValue());
    }

    public long getCreationTimeNanoseconds() {
        return this.epochNanos(this.getCreationTime().getLongValue());
    }

    public long getFileSize() {
        return this.getFileSizeHigh() << 32 | this.getFileSizeLow();
    }

    private long epochNanos(long windowsNanoChunks) {
        return windowsNanoChunks * 100L - -6802270473709551616L;
    }

    public static long asNanoSeconds(long seconds) {
        return (seconds * 1000L + -6802270473709551L) * 10L;
    }

    public class HackyFileTime {
        private final Struct.UnsignedLong dwHighDateTime;
        private final Struct.UnsignedLong dwLowDateTime;

        public HackyFileTime(Struct.UnsignedLong high, Struct.UnsignedLong low) {
            this.dwHighDateTime = high;
            this.dwLowDateTime = low;
        }

        public long getLowDateTime() {
            return this.dwLowDateTime.longValue();
        }

        public long getHighDateTime() {
            return this.dwHighDateTime.longValue();
        }

        public long getLongValue() {
            return (this.getHighDateTime() & 0xFFFFFFFFL) << 32 | this.getLowDateTime() & 0xFFFFFFFFL;
        }
    }
}

