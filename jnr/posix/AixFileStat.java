
package jnr.posix;

import jnr.ffi.NativeType;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseFileStat;
import jnr.posix.NanosecondFileStat;
import jnr.posix.NativePOSIX;

public final class AixFileStat
extends BaseFileStat
implements NanosecondFileStat {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    public AixFileStat(NativePOSIX posix) {
        super(posix, layout);
    }

    @Override
    public long atime() {
        return AixFileStat.layout.st_atime.get(this.memory);
    }

    @Override
    public long blocks() {
        return AixFileStat.layout.st_blocks.get(this.memory);
    }

    @Override
    public long blockSize() {
        return AixFileStat.layout.st_blksize.get(this.memory);
    }

    @Override
    public long ctime() {
        return AixFileStat.layout.st_ctime.get(this.memory);
    }

    @Override
    public long dev() {
        return AixFileStat.layout.st_dev.get(this.memory);
    }

    @Override
    public int gid() {
        return (int)AixFileStat.layout.st_gid.get(this.memory);
    }

    @Override
    public long ino() {
        return AixFileStat.layout.st_ino.get(this.memory);
    }

    @Override
    public int mode() {
        return (int)AixFileStat.layout.st_mode.get(this.memory) & 0xFFFF;
    }

    @Override
    public long mtime() {
        return AixFileStat.layout.st_mtime.get(this.memory);
    }

    @Override
    public int nlink() {
        return AixFileStat.layout.st_nlink.get(this.memory);
    }

    @Override
    public long rdev() {
        return AixFileStat.layout.st_rdev.get(this.memory);
    }

    @Override
    public long st_size() {
        return AixFileStat.layout.st_size.get(this.memory);
    }

    @Override
    public int uid() {
        return (int)AixFileStat.layout.st_uid.get(this.memory);
    }

    @Override
    public long aTimeNanoSecs() {
        return AixFileStat.layout.st_atime_n.get(this.memory);
    }

    @Override
    public long cTimeNanoSecs() {
        return AixFileStat.layout.st_ctime_n.get(this.memory);
    }

    @Override
    public long mTimeNanoSecs() {
        return AixFileStat.layout.st_mtime_n.get(this.memory);
    }

    private static final class Layout
    extends StructLayout {
        public final StructLayout.Unsigned64 st_dev = new StructLayout.Unsigned64();
        public final StructLayout.Signed64 st_ino = new StructLayout.Signed64();
        public final StructLayout.Unsigned32 st_mode = new StructLayout.Unsigned32();
        public final StructLayout.Signed16 st_nlink = new StructLayout.Signed16();
        public final StructLayout.Unsigned16 st_flag = new StructLayout.Unsigned16();
        public final StructLayout.Unsigned32 st_uid = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_gid = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned64 st_rdev = new StructLayout.Unsigned64();
        public final StructLayout.Signed64 st_size = new StructLayout.Signed64();
        public final StructLayout.Signed64 st_atime = new StructLayout.Signed64();
        public final StructLayout.Signed32 st_atime_n = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_pad1 = new StructLayout.Signed32();
        public final StructLayout.Signed64 st_mtime = new StructLayout.Signed64();
        public final StructLayout.Signed32 st_mtime_n = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_pad2 = new StructLayout.Signed32();
        public final StructLayout.Signed64 st_ctime = new StructLayout.Signed64();
        public final StructLayout.Signed32 st_ctime_n = new StructLayout.Signed32();
        public final StructLayout.Signed32 st_pad3 = new StructLayout.Signed32();
        public final StructLayout.Unsigned64 st_blksize = new StructLayout.Unsigned64();
        public final StructLayout.Unsigned64 st_blocks = new StructLayout.Unsigned64();
        public final StructLayout.Signed32 st_vfstype = new StructLayout.Signed32();
        public final StructLayout.Unsigned32 st_vfs = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_type = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 st_gen = new StructLayout.Unsigned32();
        public final StructLayout.Padding st_reserved = (StructLayout)this.new StructLayout.Padding(NativeType.UINT, 11);

        private Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

