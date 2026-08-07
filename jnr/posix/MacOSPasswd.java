
package jnr.posix;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.StructLayout;
import jnr.posix.NativePasswd;
import jnr.posix.Passwd;

public final class MacOSPasswd
extends NativePasswd
implements Passwd {
    private static final Layout layout = new Layout(Runtime.getSystemRuntime());

    MacOSPasswd(Pointer memory) {
        super(memory);
    }

    @Override
    public String getAccessClass() {
        return MacOSPasswd.layout.pw_class.get(this.memory);
    }

    @Override
    public String getGECOS() {
        return MacOSPasswd.layout.pw_gecos.get(this.memory);
    }

    @Override
    public long getGID() {
        return MacOSPasswd.layout.pw_gid.get(this.memory);
    }

    @Override
    public String getHome() {
        return MacOSPasswd.layout.pw_dir.get(this.memory);
    }

    @Override
    public String getLoginName() {
        return MacOSPasswd.layout.pw_name.get(this.memory);
    }

    @Override
    public int getPasswdChangeTime() {
        return MacOSPasswd.layout.pw_change.intValue(this.memory);
    }

    @Override
    public String getPassword() {
        return MacOSPasswd.layout.pw_passwd.get(this.memory);
    }

    @Override
    public String getShell() {
        return MacOSPasswd.layout.pw_shell.get(this.memory);
    }

    @Override
    public long getUID() {
        return MacOSPasswd.layout.pw_uid.get(this.memory);
    }

    @Override
    public int getExpire() {
        return MacOSPasswd.layout.pw_expire.intValue(this.memory);
    }

    private static final class Layout
    extends StructLayout {
        public final StructLayout.UTF8StringRef pw_name = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_passwd = new StructLayout.UTF8StringRef();
        public final StructLayout.Signed32 pw_uid = new StructLayout.Signed32();
        public final StructLayout.Signed32 pw_gid = new StructLayout.Signed32();
        public final StructLayout.SignedLong pw_change = new StructLayout.SignedLong();
        public final StructLayout.UTF8StringRef pw_class = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_gecos = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_dir = new StructLayout.UTF8StringRef();
        public final StructLayout.UTF8StringRef pw_shell = new StructLayout.UTF8StringRef();
        public final StructLayout.SignedLong pw_expire = new StructLayout.SignedLong();

        private Layout(Runtime runtime) {
            super(runtime);
        }
    }
}

