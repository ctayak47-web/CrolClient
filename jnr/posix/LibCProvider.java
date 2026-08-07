
package jnr.posix;

import jnr.posix.Crypt;
import jnr.posix.LibC;

public interface LibCProvider {
    public LibC getLibC();

    public Crypt getCrypt();
}

