
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.constants.platform.Confstr;
import jnr.constants.platform.Pathconf;
import jnr.constants.platform.Sysconf;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.FromNativeContext;
import jnr.posix.BaseNativePOSIX;
import jnr.posix.DragonFlyFileStat;
import jnr.posix.DragonFlyPasswd;
import jnr.posix.FileStat;
import jnr.posix.LibCProvider;
import jnr.posix.MsgHdr;
import jnr.posix.NativeTimes;
import jnr.posix.POSIXHandler;
import jnr.posix.SocketMacros;
import jnr.posix.Times;
import jnr.posix.util.MethodName;

final class DragonFlyPOSIX
extends BaseNativePOSIX {
    public static final BaseNativePOSIX.PointerConverter PASSWD = new BaseNativePOSIX.PointerConverter(){

        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new DragonFlyPasswd((Pointer)arg) : null;
        }
    };

    DragonFlyPOSIX(LibCProvider libc, POSIXHandler handler) {
        super(libc, handler);
    }

    @Override
    public FileStat allocateStat() {
        return new DragonFlyFileStat(this);
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
        return Memory.allocateDirect(this.getRuntime(), 8);
    }

    @Override
    public Pointer allocatePosixSpawnattr() {
        return Memory.allocateDirect(this.getRuntime(), 8);
    }
}

