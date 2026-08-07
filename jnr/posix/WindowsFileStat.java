
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseFileStat;
import jnr.posix.NativePOSIX;

public class WindowsFileStat
extends BaseFileStat {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    public WindowsFileStat(NativePOSIX posix) {
        super(posix, layout);
    }

    @Override
    public long atime() {
        return WindowsFileStat.layout.st_atime.get(this.memory);
    }

    @Override
    public long blockSize() {
        return 512L;
    }

    @Override
    public long blocks() {
        return (WindowsFileStat.layout.st_size.get(this.memory) + 512L - 1L) / 512L;
    }

    @Override
    public long ctime() {
        return WindowsFileStat.layout.st_ctime.get(this.memory);
    }

    @Override
    public long dev() {
        return WindowsFileStat.layout.st_dev.get(this.memory);
    }

    @Override
    public int gid() {
        return WindowsFileStat.layout.st_gid.get(this.memory);
    }

    @Override
    public long ino() {
        return WindowsFileStat.layout.st_ino.get(this.memory);
    }

    @Override
    public int mode() {
        return WindowsFileStat.layout.st_mode.get(this.memory) & 0xFFFFFFED & 0xFFFF;
    }

    @Override
    public long mtime() {
        return WindowsFileStat.layout.st_mtime.get(this.memory);
    }

    @Override
    public int nlink() {
        return WindowsFileStat.layout.st_nlink.get(this.memory);
    }

    @Override
    public long rdev() {
        return WindowsFileStat.layout.st_rdev.get(this.memory);
    }

    @Override
    public long st_size() {
        return WindowsFileStat.layout.st_size.get(this.memory);
    }

    @Override
    public int uid() {
        return WindowsFileStat.layout.st_uid.get(this.memory);
    }

    @Override
    public boolean groupMember(int gid) {
        return true;
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

    public String toString() {
        return "st_dev: " + WindowsFileStat.layout.st_dev.get(this.memory) + ", st_mode: " + Integer.toOctalString(this.mode()) + ", layout.st_nlink: " + WindowsFileStat.layout.st_nlink.get(this.memory) + ", layout.st_rdev: " + WindowsFileStat.layout.st_rdev.get(this.memory) + ", layout.st_size: " + WindowsFileStat.layout.st_size.get(this.memory) + ", layout.st_uid: " + WindowsFileStat.layout.st_uid.get(this.memory) + ", layout.st_gid: " + WindowsFileStat.layout.st_gid.get(this.memory) + ", layout.st_atime: " + WindowsFileStat.layout.st_atime.get(this.memory) + ", layout.st_ctime: " + WindowsFileStat.layout.st_ctime.get(this.memory) + ", layout.st_mtime: " + WindowsFileStat.layout.st_mtime.get(this.memory) + ", layout.st_ino: " + WindowsFileStat.layout.st_ino.get(this.memory);
    }

    private static final class Layout
    extends StructLayout {
        public final StructLayout.Signed32 st_dev = new StructLayout.Signed32();
        public final StructLayout.Signed16 st_ino = new StructLayout.Signed16();
        public final StructLayout.Signed16 st_mode = new StructLayout.Signed16();
        public final StructLayout.Signed16 st_nlink = new StructLayout.Signed16();
        public final StructLayout.Signed16 st_uid = new StructLayout.Signed16();
        public final StructLayout.Signed16 st_gid = new StructLayout.Signed16();
        public final StructLayout.Signed32 st_rdev = new StructLayout.Signed32();
        public final StructLayout.Signed64 st_size = new StructLayout.Signed64();
        public final StructLayout.Signed64 st_atime = new StructLayout.Signed64();
        public final StructLayout.Signed64 st_mtime = new StructLayout.Signed64();
        public final StructLayout.Signed64 st_ctime = new StructLayout.Signed64();

        private Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

