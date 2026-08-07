
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseFileStat;
import jnr.posix.BaseNativePOSIX;
import jnr.posix.NanosecondFileStat;

public final class LinuxFileStat32
extends BaseFileStat
implements NanosecondFileStat {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    public LinuxFileStat32() {
        this(null);
    }

    public LinuxFileStat32(BaseNativePOSIX posix) {
        super(posix, layout);
    }

    @Override
    public long atime() {
        return LinuxFileStat32.layout.st_atim_sec.get(this.memory);
    }

    @Override
    public long aTimeNanoSecs() {
        return LinuxFileStat32.layout.st_atim_nsec.get(this.memory);
    }

    @Override
    public long blocks() {
        return LinuxFileStat32.layout.st_blocks.get(this.memory);
    }

    @Override
    public long blockSize() {
        return LinuxFileStat32.layout.st_blksize.get(this.memory);
    }

    @Override
    public long ctime() {
        return LinuxFileStat32.layout.st_ctim_sec.get(this.memory);
    }

    @Override
    public long cTimeNanoSecs() {
        return LinuxFileStat32.layout.st_ctim_nsec.get(this.memory);
    }

    @Override
    public long dev() {
        return LinuxFileStat32.layout.st_dev.get(this.memory);
    }

    @Override
    public int gid() {
        return LinuxFileStat32.layout.st_gid.get(this.memory);
    }

    @Override
    public long ino() {
        return LinuxFileStat32.layout.st_ino.get(this.memory);
    }

    @Override
    public int mode() {
        return LinuxFileStat32.layout.st_mode.get(this.memory) & 0xFFFF;
    }

    @Override
    public long mtime() {
        return LinuxFileStat32.layout.st_mtim_sec.get(this.memory);
    }

    @Override
    public long mTimeNanoSecs() {
        return LinuxFileStat32.layout.st_mtim_nsec.get(this.memory);
    }

    @Override
    public int nlink() {
        return LinuxFileStat32.layout.st_nlink.get(this.memory);
    }

    @Override
    public long rdev() {
        return LinuxFileStat32.layout.st_rdev.get(this.memory);
    }

    @Override
    public long st_size() {
        return LinuxFileStat32.layout.st_size.get(this.memory);
    }

    @Override
    public int uid() {
        return LinuxFileStat32.layout.st_uid.get(this.memory);
    }

    private static final class Layout
    extends StructLayout {
        public final StructLayout.Signed64 st_dev = new StructLayout.Signed64();
        public final StructLayout.Signed16 __pad1 = new StructLayout.Signed16();
        public final StructLayout.Signed32 st_ino = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_mode = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_nlink = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_uid = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_gid = new StructLayout.Signed32();
        public final StructLayout.Signed64 st_rdev = new StructLayout.Signed64();
        public final StructLayout.Signed16 __pad2 = new StructLayout.Signed16();
        public final StructLayout.Signed64 st_size = new StructLayout.Signed64();
        public final StructLayout.Signed32 st_blksize = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_blocks = new StructLayout.Signed32();
        public final StructLayout.Signed32 __unused4 = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_atim_sec = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_atim_nsec = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_mtim_sec = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_mtim_nsec = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_ctim_sec = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_ctim_nsec = new StructLayout.Signed32();
        public final StructLayout.Signed64 __unused5 = new StructLayout.Signed64();

        private Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

