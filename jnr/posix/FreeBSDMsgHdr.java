
package jnr.posix;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseIovec;
import jnr.posix.BaseMsgHdr;
import jnr.posix.CmsgHdr;
import jnr.posix.FreeBSDCmsgHdr;
import jnr.posix.NativePOSIX;

class FreeBSDMsgHdr
extends BaseMsgHdr {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    protected FreeBSDMsgHdr(NativePOSIX posix) {
        super(posix, layout);
        this.setName(null);
    }

    @Override
    CmsgHdr allocateCmsgHdrInternal(NativePOSIX posix, Pointer pointer, int len) {
        if (len > 0) {
            return new FreeBSDCmsgHdr(posix, pointer, len);
        }
        return new FreeBSDCmsgHdr(posix, pointer);
    }

    @Override
    void setControlPointer(Pointer control) {
        FreeBSDMsgHdr.layout.msg_control.set(this.memory, control);
    }

    @Override
    void setControlLen(int len) {
        FreeBSDMsgHdr.layout.msg_controllen.set(this.memory, len);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("msghdr {\n");
        buf.append("  msg_name=").append(this.getName()).append(",\n");
        buf.append("  msg_namelen=").append(this.getNameLen()).append(",\n");
        buf.append("  msg_iov=[\n");
        Pointer iovp = FreeBSDMsgHdr.layout.msg_iov.get(this.memory);
        int numIov = this.getIovLen();
        for (int i = 0; i < numIov; ++i) {
            Pointer eachp = iovp.slice(i * BaseIovec.layout.size());
            buf.append(new BaseIovec(this.posix, eachp).toString("    "));
            if (i < numIov - 1) {
                buf.append(",\n");
                continue;
            }
            buf.append("\n");
        }
        buf.append("  ],\n");
        buf.append("  msg_control=[\n");
        CmsgHdr[] controls = this.getControls();
        for (int i = 0; i < controls.length; ++i) {
            buf.append(((FreeBSDCmsgHdr)controls[i]).toString("    "));
            if (i < controls.length - 1) {
                buf.append(",\n");
                continue;
            }
            buf.append("\n");
        }
        buf.append("  ],\n");
        buf.append("  msg_controllen=").append(FreeBSDMsgHdr.layout.msg_controllen.get(this.memory)).append("\n");
        buf.append("  msg_iovlen=").append(this.getIovLen()).append(",\n");
        buf.append("  msg_flags=").append(this.getFlags()).append(",\n");
        buf.append("}");
        return buf.toString();
    }

    @Override
    void setNamePointer(Pointer name) {
        FreeBSDMsgHdr.layout.msg_name.set(this.memory, name);
    }

    @Override
    Pointer getNamePointer() {
        return FreeBSDMsgHdr.layout.msg_name.get(this.memory);
    }

    @Override
    void setNameLen(int len) {
        FreeBSDMsgHdr.layout.msg_namelen.set(this.memory, len);
    }

    @Override
    int getNameLen() {
        return (int)FreeBSDMsgHdr.layout.msg_namelen.get(this.memory);
    }

    @Override
    void setIovPointer(Pointer iov) {
        FreeBSDMsgHdr.layout.msg_iov.set(this.memory, iov);
    }

    @Override
    Pointer getIovPointer() {
        return FreeBSDMsgHdr.layout.msg_iov.get(this.memory);
    }

    @Override
    void setIovLen(int len) {
        FreeBSDMsgHdr.layout.msg_iovlen.set(this.memory, len);
    }

    @Override
    int getIovLen() {
        return FreeBSDMsgHdr.layout.msg_iovlen.get(this.memory);
    }

    @Override
    Pointer getControlPointer() {
        return FreeBSDMsgHdr.layout.msg_control.get(this.memory);
    }

    @Override
    public int getControlLen() {
        return (int)FreeBSDMsgHdr.layout.msg_controllen.get(this.memory);
    }

    @Override
    public void setFlags(int flags) {
        FreeBSDMsgHdr.layout.msg_flags.set(this.memory, flags);
    }

    @Override
    public int getFlags() {
        return FreeBSDMsgHdr.layout.msg_flags.get(this.memory);
    }

    public static class Layout
    extends StructLayout {
        public final StructLayout.Pointer msg_name = new StructLayout.Pointer();
        public final StructLayout.socklen_t msg_namelen = new StructLayout.socklen_t();
        public final StructLayout.Pointer msg_iov = new StructLayout.Pointer();
        public final StructLayout.Signed32 msg_iovlen = new StructLayout.Signed32();
        public final StructLayout.Pointer msg_control = new StructLayout.Pointer();
        public final StructLayout.socklen_t msg_controllen = new StructLayout.socklen_t();
        public final StructLayout.Signed32 msg_flags = new StructLayout.Signed32();

        protected Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

