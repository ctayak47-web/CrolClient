
package jnr.posix;

import jnr.ffi.Pointer;

public interface SocketMacros {
    public int CMSG_SPACE(int var1);

    public int CMSG_LEN(int var1);

    public Pointer CMSG_DATA(Pointer var1);
}

