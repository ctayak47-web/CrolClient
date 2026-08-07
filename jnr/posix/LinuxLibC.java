
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.NulTerminate;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Transient;
import jnr.ffi.types.off_t;
import jnr.posix.FileStat;
import jnr.posix.UnixLibC;

public interface LinuxLibC
extends UnixLibC {
    public int __fxstat(int var1, int var2, @Out @Transient FileStat var3);

    public int __lxstat(int var1, CharSequence var2, @Out @Transient FileStat var3);

    public int __lxstat(int var1, @NulTerminate @In ByteBuffer var2, @Out @Transient FileStat var3);

    public int __xstat(int var1, CharSequence var2, @Out @Transient FileStat var3);

    public int __xstat(int var1, @NulTerminate @In ByteBuffer var2, @Out @Transient FileStat var3);

    public int __fxstat64(int var1, int var2, @Out @Transient FileStat var3);

    public int __lxstat64(int var1, CharSequence var2, @Out @Transient FileStat var3);

    public int __lxstat64(int var1, @NulTerminate @In ByteBuffer var2, @Out @Transient FileStat var3);

    public int __xstat64(int var1, CharSequence var2, @Out @Transient FileStat var3);

    public int __xstat64(int var1, @NulTerminate @In ByteBuffer var2, @Out @Transient FileStat var3);

    public int posix_fadvise(int var1, @off_t long var2, @off_t long var4, int var6);
}

