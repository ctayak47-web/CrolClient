
package jnr.posix;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseCmsgHdr;
import jnr.posix.NativePOSIX;

class FreeBSDCmsgHdr
extends BaseCmsgHdr {
    public static final Layout layout = new Layout(Runtime.getSystemRuntime());

    public FreeBSDCmsgHdr(NativePOSIX posix, Pointer memory) {
        super(posix, memory);
    }

    public FreeBSDCmsgHdr(NativePOSIX posix, Pointer memory, int totalLen) {
        super(posix, memory, totalLen);
    }

    @Override
    public void setLevel(int level) {
        FreeBSDCmsgHdr.layout.cmsg_level.set(this.memory, level);
    }

    @Override
    public int getLevel() {
        return FreeBSDCmsgHdr.layout.cmsg_level.get(this.memory);
    }

    @Override
    public void setType(int type) {
        FreeBSDCmsgHdr.layout.cmsg_type.set(this.memory, type);
    }

    @Override
    public int getType() {
        return FreeBSDCmsgHdr.layout.cmsg_type.get(this.memory);
    }

    @Override
    public int getLen() {
        return (int)FreeBSDCmsgHdr.layout.cmsg_len.get(this.memory);
    }

    @Override
    void setLen(int len) {
        FreeBSDCmsgHdr.layout.cmsg_len.set(this.memory, len);
    }

    public String toString(String indent) {
        StringBuffer buf = new StringBuffer();
        buf.append(indent).append("cmsg {\n");
        buf.append(indent).append("  cmsg_len=").append(FreeBSDCmsgHdr.layout.cmsg_len.get(this.memory)).append("\n");
        buf.append(indent).append("  cmsg_level=").append(FreeBSDCmsgHdr.layout.cmsg_level.get(this.memory)).append("\n");
        buf.append(indent).append("  cmsg_type=").append(FreeBSDCmsgHdr.layout.cmsg_type.get(this.memory)).append("\n");
        buf.append(indent).append("  cmsg_data=").append(this.getData()).append("\n");
        buf.append(indent).append("}");
        return buf.toString();
    }

    public String toString() {
        return this.toString("");
    }

    public static class Layout
    extends StructLayout {
        public final StructLayout.socklen_t cmsg_len = new StructLayout.socklen_t();
        public final StructLayout.Signed32 cmsg_level = new StructLayout.Signed32();
        public final StructLayout.Signed32 cmsg_type = new StructLayout.Signed32();

        protected Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

