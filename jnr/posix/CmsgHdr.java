
package jnr.posix;

import java.nio.ByteBuffer;

public interface CmsgHdr {
    public void setLevel(int var1);

    public int getLevel();

    public void setType(int var1);

    public int getType();

    public void setData(ByteBuffer var1);

    public ByteBuffer getData();

    public int getLen();
}

