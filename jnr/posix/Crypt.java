
package jnr.posix;

import jnr.ffi.Pointer;

public interface Crypt {
    public CharSequence crypt(CharSequence var1, CharSequence var2);

    public Pointer crypt(byte[] var1, byte[] var2);
}

