
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.constants.platform.Confstr;
import jnr.constants.platform.Pathconf;
import jnr.constants.platform.Sysconf;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Memory;
import jnr.ffi.Platform;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.FromNativeContext;
import jnr.posix.BaseNativePOSIX;
import jnr.posix.FileStat;
import jnr.posix.LibCProvider;
import jnr.posix.MacOSFileStat;
import jnr.posix.MacOSFileStat64Inode;
import jnr.posix.MacOSMsgHdr;
import jnr.posix.MacOSPasswd;
import jnr.posix.MacOSSocketMacros;
import jnr.posix.MsgHdr;
import jnr.posix.NSGetEnviron;
import jnr.posix.NativeTimes;
import jnr.posix.POSIXHandler;
import jnr.posix.SocketMacros;
import jnr.posix.Times;

final class MacOSPOSIX
extends BaseNativePOSIX {
    private final NSGetEnviron environ;
    public static final BaseNativePOSIX.PointerConverter PASSWD = new BaseNativePOSIX.PointerConverter(){

        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new MacOSPasswd((Pointer)arg) : null;
        }
    };

    MacOSPOSIX(LibCProvider libcProvider, POSIXHandler handler) {
        super(libcProvider, handler);
        LibraryLoader<NSGetEnviron> loader = LibraryLoader.create(NSGetEnviron.class);
        loader.library("libSystem.B.dylib");
        this.environ = loader.load();
    }

    @Override
    public FileStat allocateStat() {
        if (Platform.getNativePlatform().getCPU() == Platform.CPU.AARCH64) {
            return new MacOSFileStat64Inode(this);
        }
        return new MacOSFileStat(this);
    }

    @Override
    public MsgHdr allocateMsgHdr() {
        return new MacOSMsgHdr(this);
    }

    @Override
    public Pointer allocatePosixSpawnFileActions() {
        return Memory.allocateDirect(this.getRuntime(), 8);
    }

    @Override
    public Pointer allocatePosixSpawnattr() {
        return Memory.allocateDirect(this.getRuntime(), 8);
    }

    @Override
    public SocketMacros socketMacros() {
        return MacOSSocketMacros.INSTANCE;
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
    public Pointer environ() {
        return this.environ._NSGetEnviron().getPointer(0L);
    }
}

