
package jnr.posix;

import java.nio.ByteBuffer;

public interface Iovec {
    public ByteBuffer get();

    public void set(ByteBuffer var1);
}

