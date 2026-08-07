
package jnr.posix;

import jnr.ffi.Pointer;
import jnr.posix.Passwd;

public abstract class NativePasswd
implements Passwd {
    protected final Pointer memory;

    NativePasswd(Pointer pointer) {
        this.memory = pointer;
    }
}

