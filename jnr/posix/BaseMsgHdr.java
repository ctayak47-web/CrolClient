
package jnr.posix;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.BaseIovec;
import jnr.posix.CmsgHdr;
import jnr.posix.MsgHdr;
import jnr.posix.NativePOSIX;

public abstract class BaseMsgHdr
implements MsgHdr {
    protected final NativePOSIX posix;
    protected final Pointer memory;

    protected BaseMsgHdr(NativePOSIX posix, StructLayout layout) {
        this.posix = posix;
        this.memory = posix.getRuntime().getMemoryManager().allocateTemporary(layout.size(), true);
    }

    @Override
    public void setName(String name) {
        if (name == null) {
            this.setNamePointer(null);
            this.setNameLen(0);
            return;
        }
        byte[] nameBytes = name.getBytes(Charset.forName("US-ASCII"));
        Pointer p = Runtime.getSystemRuntime().getMemoryManager().allocateTemporary(nameBytes.length, true);
        p.put(0L, nameBytes, 0, nameBytes.length);
        this.setNamePointer(p);
        this.setNameLen(nameBytes.length);
    }

    @Override
    public String getName() {
        Pointer ptr = this.getNamePointer();
        if (ptr == null) {
            return null;
        }
        return ptr.getString(0L, this.getNameLen(), Charset.forName("US-ASCII"));
    }

    @Override
    public CmsgHdr allocateControl(int dataLength) {
        CmsgHdr[] controls = this.allocateControls(new int[]{dataLength});
        return controls[0];
    }

    @Override
    public CmsgHdr[] allocateControls(int[] dataLengths) {
        CmsgHdr[] cmsgs = new CmsgHdr[dataLengths.length];
        int totalSize = 0;
        for (int i = 0; i < dataLengths.length; ++i) {
            totalSize += this.posix.socketMacros().CMSG_SPACE(dataLengths[i]);
        }
        Pointer ptr = this.posix.getRuntime().getMemoryManager().allocateDirect(totalSize);
        int offset = 0;
        for (int i = 0; i < dataLengths.length; ++i) {
            CmsgHdr each;
            int eachSpace = this.posix.socketMacros().CMSG_SPACE(dataLengths[i]);
            int len = this.posix.socketMacros().CMSG_LEN(dataLengths[i]);
            cmsgs[i] = each = this.allocateCmsgHdrInternal(this.posix, ptr.slice(offset, eachSpace), len);
            offset += eachSpace;
        }
        this.setControlPointer(ptr);
        this.setControlLen(totalSize);
        return cmsgs;
    }

    @Override
    public CmsgHdr[] getControls() {
        CmsgHdr each;
        int len = this.getControlLen();
        if (len == 0) {
            return new CmsgHdr[0];
        }
        ArrayList<CmsgHdr> control = new ArrayList<CmsgHdr>();
        Pointer controlPtr = this.getControlPointer();
        for (int offset = 0; offset < len; offset += this.posix.socketMacros().CMSG_SPACE(each.getLen())) {
            each = this.allocateCmsgHdrInternal(this.posix, controlPtr.slice(offset), -1);
            control.add(each);
        }
        return control.toArray(new CmsgHdr[control.size()]);
    }

    @Override
    public void setIov(ByteBuffer[] buffers) {
        Pointer iov = Runtime.getSystemRuntime().getMemoryManager().allocateDirect(BaseIovec.layout.size() * buffers.length);
        for (int i = 0; i < buffers.length; ++i) {
            Pointer eachIovecPtr = iov.slice(BaseIovec.layout.size() * i);
            BaseIovec eachIovec = new BaseIovec(this.posix, eachIovecPtr);
            eachIovec.set(buffers[i]);
        }
        this.setIovPointer(iov);
        this.setIovLen(buffers.length);
    }

    @Override
    public ByteBuffer[] getIov() {
        int len = this.getIovLen();
        ByteBuffer[] buffers = new ByteBuffer[len];
        Pointer iov = this.getIovPointer();
        for (int i = 0; i < len; ++i) {
            Pointer eachPtr = iov.slice(BaseIovec.layout.size() * i);
            BaseIovec eachIov = new BaseIovec(this.posix, eachPtr);
            buffers[i] = eachIov.get();
        }
        return buffers;
    }

    abstract void setNamePointer(Pointer var1);

    abstract Pointer getNamePointer();

    abstract void setNameLen(int var1);

    abstract int getNameLen();

    abstract void setIovPointer(Pointer var1);

    abstract Pointer getIovPointer();

    abstract int getIovLen();

    abstract void setIovLen(int var1);

    abstract CmsgHdr allocateCmsgHdrInternal(NativePOSIX var1, Pointer var2, int var3);

    abstract void setControlPointer(Pointer var1);

    abstract Pointer getControlPointer();

    abstract void setControlLen(int var1);
}

