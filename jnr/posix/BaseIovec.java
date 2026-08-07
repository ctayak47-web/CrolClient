
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.Iovec;
import jnr.posix.NativePOSIX;

public class BaseIovec
implements Iovec {
    public static final Layout layout = new Layout(Runtime.getSystemRuntime());
    private final NativePOSIX posix;
    protected final Pointer memory;

    public String toString(String indent) {
        StringBuffer buf = new StringBuffer();
        buf.append(indent).append("iovec {\n");
        buf.append(indent).append("  iov_base=").append(BaseIovec.layout.iov_base.get(this.memory)).append(",\n");
        buf.append(indent).append("  iov_len=").append(BaseIovec.layout.iov_len.get(this.memory)).append(",\n");
        buf.append(indent).append("}");
        return buf.toString();
    }

    protected BaseIovec(NativePOSIX posix) {
        this.posix = posix;
        this.memory = Memory.allocate(posix.getRuntime(), layout.size());
    }

    BaseIovec(NativePOSIX posix, Pointer memory) {
        this.posix = posix;
        this.memory = memory;
    }

    @Override
    public ByteBuffer get() {
        int len = this.getLen();
        byte[] bytes = new byte[len];
        BaseIovec.layout.iov_base.get(this.memory).get(0L, bytes, 0, len);
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public void set(ByteBuffer buf) {
        int len = buf.remaining();
        BaseIovec.layout.iov_base.set(this.memory, Pointer.wrap(this.posix.getRuntime(), buf));
        this.setLen(len);
    }

    protected void setLen(int len) {
        BaseIovec.layout.iov_len.set(this.memory, len);
    }

    protected int getLen() {
        return (int)BaseIovec.layout.iov_len.get(this.memory);
    }

    public static class Layout
    extends StructLayout {
        public final StructLayout.Pointer iov_base = new StructLayout.Pointer();
        public final StructLayout.size_t iov_len = new StructLayout.size_t();

        protected Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

