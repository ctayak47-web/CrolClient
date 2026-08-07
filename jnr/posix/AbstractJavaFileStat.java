
package jnr.posix;

import jnr.posix.FileStat;
import jnr.posix.POSIX;
import jnr.posix.POSIXHandler;

public abstract class AbstractJavaFileStat
implements FileStat {
    protected final POSIXHandler handler;
    protected final POSIX posix;

    public AbstractJavaFileStat(POSIX posix, POSIXHandler handler) {
        this.handler = handler;
        this.posix = posix;
    }

    @Override
    public boolean isBlockDev() {
        this.handler.unimplementedError("block device detection");
        return false;
    }

    @Override
    public boolean isCharDev() {
        return false;
    }

    @Override
    public boolean isFifo() {
        this.handler.unimplementedError("fifo file detection");
        return false;
    }

    @Override
    public boolean isNamedPipe() {
        this.handler.unimplementedError("piped file detection");
        return false;
    }

    @Override
    public boolean isSetgid() {
        this.handler.unimplementedError("setgid detection");
        return false;
    }

    @Override
    public boolean isSetuid() {
        this.handler.unimplementedError("setuid detection");
        return false;
    }

    @Override
    public boolean isSocket() {
        this.handler.unimplementedError("socket file type detection");
        return false;
    }

    @Override
    public boolean isSticky() {
        this.handler.unimplementedError("sticky bit detection");
        return false;
    }

    @Override
    public int major(long dev) {
        this.handler.unimplementedError("major device");
        return -1;
    }

    @Override
    public int minor(long dev) {
        this.handler.unimplementedError("minor device");
        return -1;
    }

    @Override
    public int nlink() {
        this.handler.unimplementedError("stat.nlink");
        return -1;
    }

    @Override
    public long rdev() {
        this.handler.unimplementedError("stat.rdev");
        return -1L;
    }

    @Override
    public int uid() {
        return -1;
    }

    @Override
    public long blocks() {
        this.handler.unimplementedError("stat.st_blocks");
        return -1L;
    }

    @Override
    public long blockSize() {
        return 4096L;
    }

    @Override
    public long dev() {
        this.handler.unimplementedError("stat.st_dev");
        return -1L;
    }

    @Override
    public String ftype() {
        if (this.isFile()) {
            return "file";
        }
        if (this.isDirectory()) {
            return "directory";
        }
        return "unknown";
    }

    @Override
    public int gid() {
        this.handler.unimplementedError("stat.st_gid");
        return -1;
    }

    @Override
    public boolean groupMember(int gid) {
        return this.posix.getgid() == gid || this.posix.getegid() == gid;
    }

    @Override
    public long ino() {
        return 0L;
    }
}

