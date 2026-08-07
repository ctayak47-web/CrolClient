
package jnr.posix;

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.StructLayout;
import jnr.posix.FileStat;
import jnr.posix.NativePOSIX;
import jnr.posix.POSIX;

public abstract class BaseFileStat
implements FileStat {
    protected final POSIX posix;
    protected final Pointer memory;

    protected BaseFileStat(NativePOSIX posix, StructLayout layout) {
        this.posix = posix;
        this.memory = Memory.allocate(posix.getRuntime(), layout.size());
    }

    @Override
    public String ftype() {
        if (this.isFile()) {
            return "file";
        }
        if (this.isDirectory()) {
            return "directory";
        }
        if (this.isCharDev()) {
            return "characterSpecial";
        }
        if (this.isBlockDev()) {
            return "blockSpecial";
        }
        if (this.isFifo()) {
            return "fifo";
        }
        if (this.isSymlink()) {
            return "link";
        }
        if (this.isSocket()) {
            return "socket";
        }
        return "unknown";
    }

    @Override
    public boolean groupMember(int gid) {
        return this.posix.getgid() == gid || this.posix.getegid() == gid;
    }

    @Override
    public boolean isBlockDev() {
        return (this.mode() & 0xF000) == 24576;
    }

    @Override
    public boolean isCharDev() {
        return (this.mode() & 0xF000) == 8192;
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
        if (this.posix.geteuid() == 0) {
            return (this.mode() & 0x49) != 0;
        }
        if (this.isOwned()) {
            return (this.mode() & 0x40) != 0;
        }
        if (this.isGroupOwned()) {
            return (this.mode() & 8) != 0;
        }
        return (this.mode() & 1) != 0;
    }

    @Override
    public boolean isExecutableReal() {
        if (this.posix.getuid() == 0) {
            return (this.mode() & 0x49) != 0;
        }
        if (this.isROwned()) {
            return (this.mode() & 0x40) != 0;
        }
        if (this.groupMember(this.gid())) {
            return (this.mode() & 8) != 0;
        }
        return (this.mode() & 1) != 0;
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
        return this.posix.geteuid() == this.uid();
    }

    @Override
    public boolean isROwned() {
        return this.posix.getuid() == this.uid();
    }

    @Override
    public boolean isReadable() {
        if (this.posix.geteuid() == 0) {
            return true;
        }
        if (this.isOwned()) {
            return (this.mode() & 0x100) != 0;
        }
        if (this.isGroupOwned()) {
            return (this.mode() & 0x20) != 0;
        }
        return (this.mode() & 4) != 0;
    }

    @Override
    public boolean isReadableReal() {
        if (this.posix.getuid() == 0) {
            return true;
        }
        if (this.isROwned()) {
            return (this.mode() & 0x100) != 0;
        }
        if (this.groupMember(this.gid())) {
            return (this.mode() & 0x20) != 0;
        }
        return (this.mode() & 4) != 0;
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
        if (this.posix.geteuid() == 0) {
            return true;
        }
        if (this.isOwned()) {
            return (this.mode() & 0x80) != 0;
        }
        if (this.isGroupOwned()) {
            return (this.mode() & 0x10) != 0;
        }
        return (this.mode() & 2) != 0;
    }

    @Override
    public boolean isWritableReal() {
        if (this.posix.getuid() == 0) {
            return true;
        }
        if (this.isROwned()) {
            return (this.mode() & 0x80) != 0;
        }
        if (this.groupMember(this.gid())) {
            return (this.mode() & 0x10) != 0;
        }
        return (this.mode() & 2) != 0;
    }

    @Override
    public int major(long dev) {
        return (int)(dev >> 24) & 0xFF;
    }

    @Override
    public int minor(long dev) {
        return (int)(dev & 0xFFFFFFL);
    }
}

