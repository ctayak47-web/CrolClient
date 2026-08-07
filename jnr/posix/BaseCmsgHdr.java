
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.ffi.Pointer;
import jnr.posix.CmsgHdr;
import jnr.posix.NativePOSIX;

abstract class BaseCmsgHdr
implements CmsgHdr {
    protected final NativePOSIX posix;
    final Pointer memory;

    protected BaseCmsgHdr(NativePOSIX posix, Pointer memory) {
        this.posix = posix;
        this.memory = memory;
    }

    protected BaseCmsgHdr(NativePOSIX posix, Pointer memory, int totalLen) {
        this.posix = posix;
        this.memory = memory;
        this.setLen(totalLen);
    }

    @Override
    public void setData(ByteBuffer data) {
        byte[] bytes = new byte[data.capacity() - data.position()];
        data.get(bytes);
        this.posix.socketMacros().CMSG_DATA(this.memory).put(0L, bytes, 0, bytes.length);
    }

    @Override
    public ByteBuffer getData() {
        int dataLen = this.getLen() - this.posix.socketMacros().CMSG_LEN(0);
        if (dataLen == 0) {
            return null;
        }
        byte[] bytes = new byte[dataLen];
        this.posix.socketMacros().CMSG_DATA(this.memory).get(0L, bytes, 0, bytes.length);
        ByteBuffer buf = ByteBuffer.allocate(bytes.length);
        buf.put(bytes);
        buf.flip();
        return buf;
    }

    abstract void setLen(int var1);
}

