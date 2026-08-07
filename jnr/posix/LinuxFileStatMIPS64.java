
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseFileStat;
import jnr.posix.LinuxPOSIX;
import jnr.posix.NanosecondFileStat;

public final class LinuxFileStatMIPS64
extends BaseFileStat
implements NanosecondFileStat {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    public LinuxFileStatMIPS64(LinuxPOSIX posix) {
        super(posix, layout);
    }

    @Override
    public long atime() {
        return LinuxFileStatMIPS64.layout.st_atime.get(this.memory);
    }

    @Override
    public long aTimeNanoSecs() {
        return LinuxFileStatMIPS64.layout.st_atimensec.get(this.memory);
    }

    @Override
    public long blockSize() {
        return LinuxFileStatMIPS64.layout.st_blksize.get(this.memory);
    }

    @Override
    public long blocks() {
        return LinuxFileStatMIPS64.layout.st_blocks.get(this.memory);
    }

    @Override
    public long ctime() {
        return LinuxFileStatMIPS64.layout.st_ctime.get(this.memory);
    }

    @Override
    public long cTimeNanoSecs() {
        return LinuxFileStatMIPS64.layout.st_ctimensec.get(this.memory);
    }

    @Override
    public long dev() {
        return LinuxFileStatMIPS64.layout.st_dev.get(this.memory);
    }

    @Override
    public int gid() {
        return (int)LinuxFileStatMIPS64.layout.st_gid.get(this.memory);
    }

    @Override
    public long ino() {
        return LinuxFileStatMIPS64.layout.st_ino.get(this.memory);
    }

    @Override
    public int mode() {
        return (int)LinuxFileStatMIPS64.layout.st_mode.get(this.memory);
    }

    @Override
    public long mtime() {
        return LinuxFileStatMIPS64.layout.st_mtime.get(this.memory);
    }

    @Override
    public long mTimeNanoSecs() {
        return LinuxFileStatMIPS64.layout.st_mtimensec.get(this.memory);
    }

    @Override
    public int nlink() {
        return (int)LinuxFileStatMIPS64.layout.st_nlink.get(this.memory);
    }

    @Override
    public long rdev() {
        return LinuxFileStatMIPS64.layout.st_rdev.get(this.memory);
    }

    @Override
    public long st_size() {
        return LinuxFileStatMIPS64.layout.st_size.get(this.memory);
    }

    @Override
    public int uid() {
        return (int)LinuxFileStatMIPS64.layout.st_uid.get(this.memory);
    }

    public static final class Layout
    extends StructLayout {
        public final StructLayout.Unsigned64 st_dev = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned32 __pad01 = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 __pad02 = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 __pad03 = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned64 st_ino = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned64 st_mode = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned32 st_nlink = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_uid = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_gid = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned64 st_rdev = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned32 __pad11 = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 __pad12 = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 __pad13 = new StructLayout.Unsigned32();
        public final StructLayout.Signed64 st_size = new StructLayout.Signed64();
        public final StructLayout.Unsigned64 st_atime = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned64 st_atimensec = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned64 st_mtime = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned64 st_mtimensec = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned64 st_ctime = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned64 st_ctimensec = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned64 st_blksize = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned32 __pad20 = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned64 st_blocks = new StructLayout.Unsigned64();

        public Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

