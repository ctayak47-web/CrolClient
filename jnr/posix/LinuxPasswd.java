
package jnr.posix;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.NativePasswd;
import jnr.posix.Passwd;

public final class LinuxPasswd
extends NativePasswd
implements Passwd {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    LinuxPasswd(Pointer memory) {
        super(memory);
    }

    @Override
    public String getAccessClass() {
        return "";
    }

    @Override
    public String getGECOS() {
        return LinuxPasswd.layout.pw_gecos.get(this.memory);
    }

    @Override
    public long getGID() {
        return LinuxPasswd.layout.pw_gid.get(this.memory);
    }

    @Override
    public String getHome() {
        return LinuxPasswd.layout.pw_dir.get(this.memory);
    }

    @Override
    public String getLoginName() {
        return LinuxPasswd.layout.pw_name.get(this.memory);
    }

    @Override
    public String getPassword() {
        return LinuxPasswd.layout.pw_passwd.get(this.memory);
    }

    @Override
    public String getShell() {
        return LinuxPasswd.layout.pw_shell.get(this.memory);
    }

    @Override
    public long getUID() {
        return LinuxPasswd.layout.pw_uid.get(this.memory);
    }

    @Override
    public int getPasswdChangeTime() {
        return 0;
    }

    @Override
    public int getExpire() {
        return Integer.MAX_VALUE;
    }

    private static final class Layout
    extends StructLayout {
        public final StructLayout.UTF8StringRef pw_name = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_passwd = new StructLayout.UTF8StringRef();
        public final StructLayout.Signed32 pw_uid = new StructLayout.Signed32();
        public final StructLayout.Signed32 pw_gid = new StructLayout.Signed32();
        public final StructLayout.UTF8StringRef pw_gecos = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_dir = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_shell = new StructLayout.UTF8StringRef();

        private Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

