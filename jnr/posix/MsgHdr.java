
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.posix.CmsgHdr;

public interface MsgHdr {
    public void setName(String var1);

    public String getName();

    public void setIov(ByteBuffer[] var1);

    public ByteBuffer[] getIov();

    public void setFlags(int var1);

    public int getFlags();

    public CmsgHdr allocateControl(int var1);

    public CmsgHdr[] allocateControls(int[] var1);

    public CmsgHdr[] getControls();

    public int getControlLen();
}

