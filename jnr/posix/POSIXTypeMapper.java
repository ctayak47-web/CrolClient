
package jnr.posix;

import jnr.constants.Constant;
import jnr.ffi.Platform;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;
import jnr.posix.AixPOSIX;
import jnr.posix.BaseNativePOSIX;
import jnr.posix.DragonFlyPOSIX;
import jnr.posix.FileStat;
import jnr.posix.FreeBSDPOSIX;
import jnr.posix.Group;
import jnr.posix.HANDLE;
import jnr.posix.LinuxPOSIX;
import jnr.posix.MacOSPOSIX;
import jnr.posix.MsgHdr;
import jnr.posix.NativeTimes;
import jnr.posix.OpenBSDPOSIX;
import jnr.posix.Passwd;
import jnr.posix.SolarisPOSIX;
import jnr.posix.WString;
import jnr.posix.WindowsPOSIX;
import jnr.posix.util.Platform;

final class POSIXTypeMapper
implements TypeMapper {
    public static final TypeMapper INSTANCE = new POSIXTypeMapper();

    private POSIXTypeMapper() {
    }

    @Override
    public FromNativeConverter getFromNativeConverter(Class klazz) {
        if (Passwd.class.isAssignableFrom(klazz)) {
            if (Platform.IS_MAC) {
                return MacOSPOSIX.PASSWD;
            }
            if (Platform.IS_LINUX) {
                return LinuxPOSIX.PASSWD;
            }
            if (Platform.IS_SOLARIS) {
                return SolarisPOSIX.PASSWD;
            }
            if (Platform.IS_FREEBSD) {
                return FreeBSDPOSIX.PASSWD;
            }
            if (Platform.IS_DRAGONFLY) {
                return DragonFlyPOSIX.PASSWD;
            }
            if (Platform.IS_OPENBSD) {
                return OpenBSDPOSIX.PASSWD;
            }
            if (Platform.IS_WINDOWS) {
                return WindowsPOSIX.PASSWD;
            }
            if (jnr.ffi.Platform.getNativePlatform().getOS().equals((Object)Platform.OS.AIX)) {
                return AixPOSIX.PASSWD;
            }
            return null;
        }
        if (Group.class.isAssignableFrom(klazz)) {
            return BaseNativePOSIX.GROUP;
        }
        if (HANDLE.class.isAssignableFrom(klazz)) {
            return HANDLE.Converter;
        }
        return null;
    }

    @Override
    public ToNativeConverter getToNativeConverter(Class klazz) {
        if (FileStat.class.isAssignableFrom(klazz)) {
            return BaseNativePOSIX.FileStatConverter;
        }
        if (NativeTimes.class.isAssignableFrom(klazz)) {
            return BaseNativePOSIX.TimesConverter;
        }
        if (Constant.class.isAssignableFrom(klazz)) {
            return BaseNativePOSIX.ConstantConverter;
        }
        if (WString.class.isAssignableFrom(klazz)) {
            return WString.Converter;
        }
        if (HANDLE.class.isAssignableFrom(klazz)) {
            return HANDLE.Converter;
        }
        if (MsgHdr.class.isAssignableFrom(klazz)) {
            return BaseNativePOSIX.MsgHdrConverter;
        }
        return null;
    }

    public final ToNativeConverter getToNativeConverter(Class klazz, ToNativeContext context) {
        return this.getToNativeConverter(klazz);
    }

    public final FromNativeConverter getFromNativeConverter(Class klazz, FromNativeContext context) {
        return this.getFromNativeConverter(klazz);
    }
}

