
package jnr.posix;

import java.util.ArrayList;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.Group;
import jnr.posix.NativeGroup;

public final class DefaultNativeGroup
extends NativeGroup
implements Group {
    static final Layout layout = new Layout(Runtime.getSystemRuntime());
    private final Pointer memory;

    DefaultNativeGroup(Pointer memory) {
        super(memory.getRuntime(), layout);
        this.memory = memory;
    }

    @Override
    public String getName() {
        return DefaultNativeGroup.layout.gr_name.get(this.memory);
    }

    @Override
    public String getPassword() {
        return DefaultNativeGroup.layout.gr_passwd.get(this.memory);
    }

    @Override
    public long getGID() {
        return DefaultNativeGroup.layout.gr_gid.get(this.memory);
    }

    @Override
    public String[] getMembers() {
        Pointer member;
        ArrayList<String> lst = new ArrayList<String>();
        Pointer ptr = DefaultNativeGroup.layout.gr_mem.get(this.memory);
        int ptrSize = this.runtime.addressSize();
        int i = 0;
        while ((member = ptr.getPointer(i)) != null) {
            lst.add(member.getString(0L));
            i += ptrSize;
        }
        return lst.toArray(new String[lst.size()]);
    }

    static final class Layout
    extends StructLayout {
        public final StructLayout.UTF8StringRef gr_name = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef gr_passwd = new StructLayout.UTF8StringRef();
        public final StructLayout.Signed32 gr_gid = new StructLayout.Signed32();
        public final StructLayout.Pointer gr_mem = new StructLayout.Pointer();

        public Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

