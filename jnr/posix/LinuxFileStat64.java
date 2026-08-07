
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseFileStat;
import jnr.posix.LinuxPOSIX;
import jnr.posix.NanosecondFileStat;

public final class LinuxFileStat64
extends BaseFileStat
implements NanosecondFileStat {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    public LinuxFileStat64(LinuxPOSIX posix) {
        super(posix, layout);
    }

    @Override
    public long atime() {
        return LinuxFileStat64.layout.st_atime.get(this.memory);
    }

    @Override
    public long aTimeNanoSecs() {
        return LinuxFileStat64.layout.st_atimensec.get(this.memory);
    }

    @Override
    public long blockSize() {
        return LinuxFileStat64.layout.st_blksize.get(this.memory);
    }

    @Override
    public long blocks() {
        return LinuxFileStat64.layout.st_blocks.get(this.memory);
    }

    @Override
    public long ctime() {
        return LinuxFileStat64.layout.st_ctime.get(this.memory);
    }

    @Override
    public long cTimeNanoSecs() {
        return LinuxFileStat64.layout.st_ctimensec.get(this.memory);
    }

    @Override
    public long dev() {
        return LinuxFileStat64.layout.st_dev.get(this.memory);
    }

    @Override
    public int gid() {
        return (int)LinuxFileStat64.layout.st_gid.get(this.memory);
    }

    @Override
    public long ino() {
        return LinuxFileStat64.layout.st_ino.get(this.memory);
    }

    @Override
    public int mode() {
        return (int)LinuxFileStat64.layout.st_mode.get(this.memory);
    }

    @Override
    public long mtime() {
        return LinuxFileStat64.layout.st_mtime.get(this.memory);
    }

    @Override
    public long mTimeNanoSecs() {
        return LinuxFileStat64.layout.st_mtimensec.get(this.memory);
    }

    @Override
    public int nlink() {
        return (int)LinuxFileStat64.layout.st_nlink.get(this.memory);
    }

    @Override
    public long rdev() {
        return LinuxFileStat64.layout.st_rdev.get(this.memory);
    }

    @Override
    public long st_size() {
        return LinuxFileStat64.layout.st_size.get(this.memory);
    }

    @Override
    public int uid() {
        return (int)LinuxFileStat64.layout.st_uid.get(this.memory);
    }

    public static final class Layout
    extends StructLayout {
        public final StructLayout.dev_t st_dev = new StructLayout.dev_t();
        public final StructLayout.ino_t st_ino = new StructLayout.ino_t();
        public final StructLayout.nlink_t st_nlink = new StructLayout.nlink_t();
        public final StructLayout.mode_t st_mode = new StructLayout.mode_t();
        public final StructLayout.uid_t st_uid = new StructLayout.uid_t();
        public final StructLayout.gid_t st_gid = new StructLayout.gid_t();
        public final StructLayout.dev_t st_rdev = new StructLayout.dev_t();
        public final StructLayout.off_t st_size = new StructLayout.off_t();
        public final StructLayout.blksize_t st_blksize = new StructLayout.blksize_t();
        public final StructLayout.blkcnt_t st_blocks = new StructLayout.blkcnt_t();
        public final StructLayout.time_t st_atime = new StructLayout.time_t();
        public final StructLayout.SignedLong st_atimensec = new StructLayout.SignedLong();
        public final StructLayout.time_t st_mtime = new StructLayout.time_t();
        public final StructLayout.SignedLong st_mtimensec = new StructLayout.SignedLong();
        public final StructLayout.time_t st_ctime = new StructLayout.time_t();
        public final StructLayout.SignedLong st_ctimensec = new StructLayout.SignedLong();
        public final StructLayout.Signed64 __unused4 = new StructLayout.Signed64();
        public final StructLayout.Signed64 __unused5 = new StructLayout.Signed64();
        public final StructLayout.Signed64 __unused6 = new StructLayout.Signed64();

        public Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

