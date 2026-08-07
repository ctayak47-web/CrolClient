
package jnr.posix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.text.ParsePosition;
import jnr.constants.platform.Confstr;
import jnr.constants.platform.Pathconf;
import jnr.constants.platform.Sysconf;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.FromNativeContext;
import jnr.posix.BaseNativePOSIX;
import jnr.posix.FileStat;
import jnr.posix.FreeBSDFileStat;
import jnr.posix.FreeBSDFileStat12;
import jnr.posix.FreeBSDMsgHdr;
import jnr.posix.FreeBSDPasswd;
import jnr.posix.FreeBSDSocketMacros;
import jnr.posix.LibCProvider;
import jnr.posix.MsgHdr;
import jnr.posix.NativeTimes;
import jnr.posix.POSIXHandler;
import jnr.posix.SocketMacros;
import jnr.posix.Times;

final class FreeBSDPOSIX
extends BaseNativePOSIX {
    private final int freebsdVersion;
    public static final BaseNativePOSIX.PointerConverter PASSWD = new BaseNativePOSIX.PointerConverter(){

        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new FreeBSDPasswd((Pointer)arg) : null;
        }
    };

    FreeBSDPOSIX(LibCProvider libc, POSIXHandler handler) {
        super(libc, handler);
        int parsed_version = 0;
        try {
            Process p = Runtime.getRuntime().exec("/bin/freebsd-version -u");
            String version = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
            if (p.waitFor() == 0 && version != null) {
                NumberFormat fmt = NumberFormat.getIntegerInstance();
                fmt.setGroupingUsed(false);
                parsed_version = fmt.parse(version, new ParsePosition(0)).intValue();
            }
        }
        catch (Exception exception) {
            
        }
        this.freebsdVersion = parsed_version;
    }

    @Override
    public FileStat allocateStat() {
        if (this.freebsdVersion >= 12) {
            return new FreeBSDFileStat12(this);
        }
        return new FreeBSDFileStat(this);
    }

    @Override
    public MsgHdr allocateMsgHdr() {
        return new FreeBSDMsgHdr(this);
    }

    @Override
    public SocketMacros socketMacros() {
        return FreeBSDSocketMacros.INSTANCE;
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

