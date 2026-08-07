
package jnr.posix;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.NativePasswd;
import jnr.posix.Passwd;

public class AixPasswd
extends NativePasswd
implements Passwd {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    AixPasswd(Pointer memory) {
        super(memory);
    }

    @Override
    public String getAccessClass() {
        return "unknown";
    }

    @Override
    public String getGECOS() {
        return AixPasswd.layout.pw_gecos.get(this.memory);
    }

    @Override
    public long getGID() {
        return AixPasswd.layout.pw_gid.get(this.memory);
    }

    @Override
    public String getHome() {
        return AixPasswd.layout.pw_dir.get(this.memory);
    }

    @Override
    public String getLoginName() {
        return AixPasswd.layout.pw_name.get(this.memory);
    }

    @Override
    public int getPasswdChangeTime() {
        return 0;
    }

    @Override
    public String getPassword() {
        return AixPasswd.layout.pw_passwd.get(this.memory);
    }

    @Override
    public String getShell() {
        return AixPasswd.layout.pw_shell.get(this.memory);
    }

    @Override
    public long getUID() {
        return AixPasswd.layout.pw_uid.get(this.memory);
    }

    @Override
    public int getExpire() {
        return Integer.MAX_VALUE;
    }

    private static final class Layout
    extends StructLayout {
        public final StructLayout.UTF8StringRef pw_name = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_passwd = new StructLayout.UTF8StringRef();
        public final StructLayout.uid_t pw_uid = new StructLayout.uid_t();
        public final StructLayout.gid_t pw_gid = new StructLayout.gid_t();
        public final StructLayout.UTF8StringRef pw_gecos = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_dir = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_shell = new StructLayout.UTF8StringRef();

        private Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

