
package jnr.posix;

import jnr.constants.platform.PosixFadvise;
import jnr.posix.POSIX;

public interface Linux
extends POSIX {
    public int ioprio_get(int var1, int var2);

    public int ioprio_set(int var1, int var2, int var3);

    public int posix_fadvise(int var1, long var2, long var4, PosixFadvise var6);
}

