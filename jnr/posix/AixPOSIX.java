
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.constants.platform.Confstr;
import jnr.constants.platform.Fcntl;
import jnr.constants.platform.Pathconf;
import jnr.constants.platform.Sysconf;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.FromNativeContext;
import jnr.posix.AixFileStat;
import jnr.posix.AixFlock;
import jnr.posix.AixPasswd;
import jnr.posix.AixTimeval;
import jnr.posix.BaseNativePOSIX;
import jnr.posix.FileStat;
import jnr.posix.Flock;
import jnr.posix.LibCProvider;
import jnr.posix.MsgHdr;
import jnr.posix.NativeTimes;
import jnr.posix.POSIXHandler;
import jnr.posix.SocketMacros;
import jnr.posix.Times;
import jnr.posix.Timeval;
import jnr.posix.util.MethodName;

final class AixPOSIX
extends BaseNativePOSIX {
    public static final BaseNativePOSIX.PointerConverter PASSWD = new BaseNativePOSIX.PointerConverter(){

        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new AixPasswd((Pointer)arg) : null;
        }
    };

    AixPOSIX(LibCProvider libc, POSIXHandler handler) {
        super(libc, handler);
    }

    @Override
    public FileStat allocateStat() {
        return new AixFileStat(this);
    }

    @Override
    public MsgHdr allocateMsgHdr() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return null;
    }

    @Override
    public SocketMacros socketMacros() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return null;
    }

    @Override
    public long sysconf(Sysconf name) {
        return this.libc().sysconf(name);
    }

    @Override
    public int confstr(Confstr name, ByteBuffer buf, int len) {
        return this.libc().confstr(name, buf, len);
    }

    @Override
    public int fpathconf(int fd, Pathconf name) {
        return this.libc().fpathconf(fd, name);
    }

    @Override
    public Times times() {
        return NativeTimes.times(this);
    }

    @Override
    public Pointer allocatePosixSpawnFileActions() {
        return Memory.allocateDirect(this.getRuntime(), 4);
    }

    @Override
    public Pointer allocatePosixSpawnattr() {
        return Memory.allocateDirect(this.getRuntime(), 60);
    }

    @Override
    public int flock(int fd, int operation) {
        int cmd = Fcntl.F_SETLKW.intValue();
        short type = 0;
        if ((operation & FlockFlags.LOCK_SH.intValue()) != 0) {
            type = (short)Fcntl.F_RDLCK.intValue();
        } else if ((operation & FlockFlags.LOCK_EX.intValue()) != 0) {
            type = (short)Fcntl.F_WRLCK.intValue();
        } else if ((operation & FlockFlags.LOCK_UN.intValue()) != 0) {
            type = (short)Fcntl.F_UNLCK.intValue();
        }
        if ((operation & FlockFlags.LOCK_NB.intValue()) != 0) {
            cmd = Fcntl.F_SETLK.intValue();
        }
        Flock flock = this.allocateFlock();
        flock.type(type);
        flock.whence((short)0);
        flock.start(0L);
        flock.len(0L);
        return this.libc().fcntl(fd, cmd, flock);
    }

    @Override
    public Timeval allocateTimeval() {
        return new AixTimeval(this.getRuntime());
    }

    public Flock allocateFlock() {
        return new AixFlock(this.getRuntime());
    }

    private static enum FlockFlags {
        LOCK_SH(1),
        LOCK_EX(2),
        LOCK_NB(4),
        LOCK_UN(8);

        private final int value;

        private FlockFlags(int value) {
            this.value = value;
        }

        public final int intValue() {
            return this.value;
        }
    }
}

