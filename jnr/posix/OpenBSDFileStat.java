
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseFileStat;
import jnr.posix.NanosecondFileStat;
import jnr.posix.NativePOSIX;

public final class OpenBSDFileStat
extends BaseFileStat
implements NanosecondFileStat {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    public OpenBSDFileStat(NativePOSIX posix) {
        super(posix, layout);
    }

    @Override
    public long atime() {
        return OpenBSDFileStat.layout.st_atime.get(this.memory);
    }

    @Override
    public long blocks() {
        return OpenBSDFileStat.layout.st_blocks.get(this.memory);
    }

    @Override
    public long blockSize() {
        return OpenBSDFileStat.layout.st_blksize.get(this.memory);
    }

    @Override
    public long ctime() {
        return OpenBSDFileStat.layout.st_ctime.get(this.memory);
    }

    @Override
    public long dev() {
        return OpenBSDFileStat.layout.st_dev.get(this.memory);
    }

    @Override
    public int gid() {
        return (int)OpenBSDFileStat.layout.st_gid.get(this.memory);
    }

    @Override
    public long ino() {
        return OpenBSDFileStat.layout.st_ino.get(this.memory);
    }

    @Override
    public int mode() {
        return (int)(OpenBSDFileStat.layout.st_mode.get(this.memory) & 0xFFFFL);
    }

    @Override
    public long mtime() {
        return OpenBSDFileStat.layout.st_mtime.get(this.memory);
    }

    @Override
    public int nlink() {
        return (int)OpenBSDFileStat.layout.st_nlink.get(this.memory);
    }

    @Override
    public long rdev() {
        return OpenBSDFileStat.layout.st_rdev.get(this.memory);
    }

    @Override
    public long st_size() {
        return OpenBSDFileStat.layout.st_size.get(this.memory);
    }

    @Override
    public int uid() {
        return (int)OpenBSDFileStat.layout.st_uid.get(this.memory);
    }

    @Override
    public long aTimeNanoSecs() {
        return OpenBSDFileStat.layout.st_atimensec.get(this.memory);
    }

    @Override
    public long cTimeNanoSecs() {
        return OpenBSDFileStat.layout.st_ctimensec.get(this.memory);
    }

    @Override
    public long mTimeNanoSecs() {
        return OpenBSDFileStat.layout.st_mtimensec.get(this.memory);
    }

    private static final class Layout
    extends StructLayout {
        public final StructLayout.Unsigned32 st_mode = new StructLayout.Unsigned32();
        public final dev_t st_dev = new dev_t();
        public final StructLayout.Unsigned64 st_ino = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned32 st_nlink = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_uid = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_gid = new StructLayout.Unsigned32();
        public final dev_t st_rdev = new dev_t();
        public final time_t st_atime = new time_t();
        public final StructLayout.SignedLong st_atimensec = new StructLayout.SignedLong();
        public final time_t st_mtime = new time_t();
        public final StructLayout.SignedLong st_mtimensec = new StructLayout.SignedLong();
        public final time_t st_ctime = new time_t();
        public final StructLayout.SignedLong st_ctimensec = new StructLayout.SignedLong();
        public final StructLayout.Signed64 st_size = new StructLayout.Signed64();
        public final StructLayout.Signed64 st_blocks = new StructLayout.Signed64();
        public final StructLayout.Unsigned32 st_blksize = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_flags = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_gen = new StructLayout.Unsigned32();
        public final time_t st_birthtime = new time_t();
        public final StructLayout.SignedLong st_birthtimensec = new StructLayout.SignedLong();

        private Layout(Runtime runtime) {
            super(runtime);
        }

        public final class dev_t
        extends StructLayout.Signed32 {
        }

        public final class time_t
        extends StructLayout.Signed64 {
        }
    }
}

