
package jnr.posix;

import jnr.posix.AbstractJavaFileStat;
import jnr.posix.FileStat;
import jnr.posix.NanosecondFileStat;
import jnr.posix.POSIX;
import jnr.posix.POSIXHandler;
import jnr.posix.util.WindowsHelpers;
import jnr.posix.windows.CommonFileInformation;

public class WindowsRawFileStat
extends AbstractJavaFileStat
implements NanosecondFileStat {
    private int st_atime;
    private long st_atimensec;
    private long st_mtimensec;
    private long st_ctimensec;
    private int st_rdev;
    private int st_dev;
    private int st_nlink;
    private int st_mode;
    private long st_size;
    private int st_ctime;
    private int st_mtime;

    public WindowsRawFileStat(POSIX posix, POSIXHandler handler) {
        super(posix, handler);
    }

    public void setup(String path, CommonFileInformation fileInfo) {
        this.st_mode = fileInfo.getMode(path);
        this.setup(fileInfo);
        if (WindowsHelpers.isDriveLetterPath(path)) {
            int letterAsNumber;
            this.st_rdev = letterAsNumber = Character.toUpperCase(path.charAt(0)) - 65;
            this.st_dev = letterAsNumber;
        }
    }

    public void setup(CommonFileInformation fileInfo) {
        long atime = fileInfo.getLastAccessTimeNanoseconds();
        this.st_atimensec = atime % 1000000000L;
        this.st_atime = (int)(atime / 1000000000L);
        long mtime = fileInfo.getLastWriteTimeNanoseconds();
        this.st_mtimensec = mtime % 1000000000L;
        this.st_mtime = (int)(mtime / 1000000000L);
        long ctime = fileInfo.getCreationTimeNanoseconds();
        this.st_ctimensec = ctime % 1000000000L;
        this.st_ctime = (int)(ctime / 1000000000L);
        this.st_size = this.isDirectory() ? 0L : fileInfo.getFileSize();
        this.st_nlink = 1;
        this.st_mode &= 0xFFFFFFED;
    }

    @Override
    public int mode() {
        return this.st_mode;
    }

    @Override
    public long mtime() {
        return this.st_mtime;
    }

    @Override
    public long atime() {
        return this.st_atime;
    }

    @Override
    public long aTimeNanoSecs() {
        return this.st_atimensec;
    }

    @Override
    public long cTimeNanoSecs() {
        return this.st_ctimensec;
    }

    @Override
    public long mTimeNanoSecs() {
        return this.st_mtimensec;
    }

    @Override
    public long dev() {
        return this.st_dev;
    }

    @Override
    public int nlink() {
        return this.st_nlink;
    }

    @Override
    public long rdev() {
        return this.st_rdev;
    }

    @Override
    public long st_size() {
        return this.st_size;
    }

    @Override
    public long ctime() {
        return this.st_ctime;
    }

    @Override
    public boolean isDirectory() {
        return (this.mode() & 0xF000) == 16384;
    }

    @Override
    public boolean isEmpty() {
        return this.st_size() == 0L;
    }

    @Override
    public boolean isExecutable() {
        if (this.isOwned()) {
            return (this.mode() & 0x40) != 0;
        }
        if (this.isGroupOwned()) {
            return (this.mode() & 8) != 0;
        }
        return (this.mode() & 1) == 0;
    }

    @Override
    public boolean isExecutableReal() {
        if (this.isROwned()) {
            return (this.mode() & 0x40) != 0;
        }
        if (this.groupMember(this.gid())) {
            return (this.mode() & 8) != 0;
        }
        return (this.mode() & 1) == 0;
    }

    @Override
    public boolean isFile() {
        return (this.mode() & 0xF000) == 32768;
    }

    @Override
    public boolean isFifo() {
        return (this.mode() & 0xF000) == 4096;
    }

    @Override
    public boolean isGroupOwned() {
        return this.groupMember(this.gid());
    }

    @Override
    public boolean isIdentical(FileStat other) {
        return this.dev() == other.dev() && this.ino() == other.ino();
    }

    @Override
    public boolean isNamedPipe() {
        return (this.mode() & 0x1000) != 0;
    }

    @Override
    public boolean isOwned() {
        return true;
    }

    @Override
    public boolean isROwned() {
        return true;
    }

    @Override
    public boolean isReadable() {
        if (this.isOwned()) {
            return (this.mode() & 0x100) != 0;
        }
        if (this.isGroupOwned()) {
            return (this.mode() & 0x20) != 0;
        }
        return (this.mode() & 4) == 0;
    }

    @Override
    public boolean isReadableReal() {
        if (this.isROwned()) {
            return (this.mode() & 0x100) != 0;
        }
        if (this.groupMember(this.gid())) {
            return (this.mode() & 0x20) != 0;
        }
        return (this.mode() & 4) == 0;
    }

    @Override
    public boolean isSetgid() {
        return (this.mode() & 0x400) != 0;
    }

    @Override
    public boolean isSetuid() {
        return (this.mode() & 0x800) != 0;
    }

    @Override
    public boolean isSocket() {
        return (this.mode() & 0xF000) == 49152;
    }

    @Override
    public boolean isSticky() {
        return (this.mode() & 0x200) != 0;
    }

    @Override
    public boolean isSymlink() {
        return (this.mode() & 0xF000) == 40960;
    }

    @Override
    public boolean isWritable() {
        if (this.isOwned()) {
            return (this.mode() & 0x80) != 0;
        }
        if (this.isGroupOwned()) {
            return (this.mode() & 0x10) != 0;
        }
        return (this.mode() & 2) == 0;
    }

    @Override
    public boolean isWritableReal() {
        if (this.isROwned()) {
            return (this.mode() & 0x80) != 0;
        }
        if (this.groupMember(this.gid())) {
            return (this.mode() & 0x10) != 0;
        }
        return (this.mode() & 2) == 0;
    }
}

