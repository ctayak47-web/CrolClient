
package jnr.posix;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.NativePasswd;
import jnr.posix.Passwd;

public class OpenBSDPasswd
extends NativePasswd
implements Passwd {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    OpenBSDPasswd(Pointer memory) {
        super(memory);
    }

    @Override
    public String getAccessClass() {
        return OpenBSDPasswd.layout.pw_class.get(this.memory);
    }

    @Override
    public String getGECOS() {
        return OpenBSDPasswd.layout.pw_gecos.get(this.memory);
    }

    @Override
    public long getGID() {
        return OpenBSDPasswd.layout.pw_gid.get(this.memory);
    }

    @Override
    public String getHome() {
        return OpenBSDPasswd.layout.pw_dir.get(this.memory);
    }

    @Override
    public String getLoginName() {
        return OpenBSDPasswd.layout.pw_name.get(this.memory);
    }

    @Override
    public int getPasswdChangeTime() {
        return OpenBSDPasswd.layout.pw_change.intValue(this.memory);
    }

    @Override
    public String getPassword() {
        return OpenBSDPasswd.layout.pw_passwd.get(this.memory);
    }

    @Override
    public String getShell() {
        return OpenBSDPasswd.layout.pw_shell.get(this.memory);
    }

    @Override
    public long getUID() {
        return OpenBSDPasswd.layout.pw_uid.get(this.memory);
    }

    @Override
    public int getExpire() {
        return OpenBSDPasswd.layout.pw_expire.intValue(this.memory);
    }

    private static final class Layout
    extends StructLayout {
        public final StructLayout.UTF8StringRef pw_name = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_passwd = new StructLayout.UTF8StringRef();
        public final StructLayout.Unsigned32 pw_uid = new StructLayout.Unsigned32();
        public final StructLayout.Unsigned32 pw_gid = new StructLayout.Unsigned32();
        public final StructLayout.Signed64 pw_change = new StructLayout.Signed64();
        public final StructLayout.UTF8StringRef pw_class = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_gecos = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_dir = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_shell = new StructLayout.UTF8StringRef();
        public final StructLayout.Signed64 pw_expire = new StructLayout.Signed64();

        private Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

