
package jnr.posix;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseCmsgHdr;
import jnr.posix.NativePOSIX;

class MacOSCmsgHdr
extends BaseCmsgHdr {
    public static final Layout layout = new Layout(Runtime.getSystemRuntime());

    public MacOSCmsgHdr(NativePOSIX posix, Pointer memory) {
        super(posix, memory);
    }

    public MacOSCmsgHdr(NativePOSIX posix, Pointer memory, int totalLen) {
        super(posix, memory, totalLen);
    }

    @Override
    public void setLevel(int level) {
        MacOSCmsgHdr.layout.cmsg_level.set(this.memory, level);
    }

    @Override
    public int getLevel() {
        return MacOSCmsgHdr.layout.cmsg_level.get(this.memory);
    }

    @Override
    public void setType(int type) {
        MacOSCmsgHdr.layout.cmsg_type.set(this.memory, type);
    }

    @Override
    public int getType() {
        return MacOSCmsgHdr.layout.cmsg_type.get(this.memory);
    }

    @Override
    public int getLen() {
        return (int)MacOSCmsgHdr.layout.cmsg_len.get(this.memory);
    }

    @Override
    void setLen(int len) {
        MacOSCmsgHdr.layout.cmsg_len.set(this.memory, len);
    }

    public String toString(String indent) {
        StringBuffer buf = new StringBuffer();
        buf.append(indent).append("cmsg {\n");
        buf.append(indent).append("  cmsg_len=").append(MacOSCmsgHdr.layout.cmsg_len.get(this.memory)).append("\n");
        buf.append(indent).append("  cmsg_level=").append(MacOSCmsgHdr.layout.cmsg_level.get(this.memory)).append("\n");
        buf.append(indent).append("  cmsg_type=").append(MacOSCmsgHdr.layout.cmsg_type.get(this.memory)).append("\n");
        buf.append(indent).append("  cmsg_data=").append(this.getData()).append("\n");
        buf.append(indent).append("}");
        return buf.toString();
    }

    public static class Layout
    extends StructLayout {
        public final StructLayout.Unsigned32 cmsg_len = new StructLayout.Unsigned32();
        public final StructLayout.Signed32 cmsg_level = new StructLayout.Signed32();
        public final StructLayout.Signed32 cmsg_type = new StructLayout.Signed32();

        protected Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

