
package jnr.unixsocket;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import jnr.constants.platform.ProtocolFamily;
import jnr.ffi.Platform;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

abstract class SockAddrUnix
extends Struct {
    private static transient Platform.OS currentOS = Platform.getNativePlatform().getOS();
    public static final int ADDR_LENGTH = 108;
    public static final int HEADER_LENGTH = 2;
    private String cachedPath;

    protected abstract Struct.UTF8String getPathField();

    protected abstract Struct.NumberField getFamilyField();

    SockAddrUnix() {
        super(Runtime.getSystemRuntime());
    }

    final void setFamily(ProtocolFamily family) {
        this.getFamilyField().set(family.intValue());
    }

    final ProtocolFamily getFamily() {
        return ProtocolFamily.valueOf(this.getFamilyField().intValue());
    }

    void setPath(String path) {
        this.cachedPath = path;
        this.getPathField().set(this.cachedPath);
    }

    void updatePath(int len) {
        if (currentOS == Platform.OS.LINUX) {
            this.cachedPath = len == 2 ? "" : this.getPath(len - 2);
        } else {
            this.cachedPath = this.getPathField().get();
            int slen = len - 2;
            if (slen <= 0) {
                this.cachedPath = "";
            } else if (slen < this.getPathField().length() && slen < this.cachedPath.length()) {
                this.cachedPath = this.cachedPath.substring(0, slen);
            }
        }
    }

    final String getPath() {
        if (null == this.cachedPath) {
            this.cachedPath = this.getPathField().get();
        }
        return this.cachedPath;
    }

    final String getPath(int len) {
        Struct.UTF8String str = this.getPathField();
        byte[] ba = new byte[str.length()];
        str.getMemory().get(str.offset(), ba, 0, len);
        if (0 != ba[0]) {
            --len;
        }
        return new String(Arrays.copyOf(ba, len), StandardCharsets.UTF_8);
    }

    int getMaximumLength() {
        return 2 + this.getPathField().length();
    }

    int length() {
        if (currentOS == Platform.OS.LINUX && null != this.cachedPath) {
            return 2 + this.cachedPath.length();
        }
        return 2 + SockAddrUnix.strlen(this.getPathField());
    }

    int getHeaderLength() {
        return 2;
    }

    static SockAddrUnix create() {
        return Platform.getNativePlatform().isBSD() ? new BSDSockAddrUnix() : new DefaultSockAddrUnix();
    }

    private static final int strlen(Struct.UTF8String str) {
        int end = str.getMemory().indexOf(str.offset(), (byte)0);
        return end >= 0 ? end : str.length();
    }

    static final class DefaultSockAddrUnix
    extends SockAddrUnix {
        public final Struct.Unsigned16 sun_family = new Struct.Unsigned16();
        public final Struct.UTF8String sun_addr = (Struct)this.new Struct.UTF8String(108);

        DefaultSockAddrUnix() {
        }

        @Override
        protected Struct.UTF8String getPathField() {
            return this.sun_addr;
        }

        @Override
        protected Struct.NumberField getFamilyField() {
            return this.sun_family;
        }
    }

    static final class BSDSockAddrUnix
    extends SockAddrUnix {
        public final Struct.Unsigned8 sun_len = new Struct.Unsigned8();
        public final Struct.Unsigned8 sun_family = new Struct.Unsigned8();
        public final Struct.UTF8String sun_addr = (Struct)this.new Struct.UTF8String(108);

        BSDSockAddrUnix() {
        }

        @Override
        public void setPath(String path) {
            super.setPath(path);
            this.sun_len.set(path.length());
        }

        @Override
        protected Struct.UTF8String getPathField() {
            return this.sun_addr;
        }

        @Override
        protected Struct.NumberField getFamilyField() {
            return this.sun_family;
        }
    }
}

