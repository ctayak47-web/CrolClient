
package jnr.posix;

import jnr.posix.AixFileStat;
import jnr.posix.UnixLibC;

public interface AixLibC
extends UnixLibC {
    public int stat64x(CharSequence var1, AixFileStat var2);

    public int fstat64x(int var1, AixFileStat var2);

    public int lstat64x(CharSequence var1, AixFileStat var2);
}

