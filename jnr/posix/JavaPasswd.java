
package jnr.posix;

import jnr.posix.JavaPOSIX;
import jnr.posix.POSIXHandler;
import jnr.posix.Passwd;

final class JavaPasswd
implements Passwd {
    private final POSIXHandler handler;

    public JavaPasswd(POSIXHandler handler) {
        this.handler = handler;
    }

    @Override
    public String getAccessClass() {
        this.handler.unimplementedError("passwd.pw_access unimplemented");
        return null;
    }

    @Override
    public String getGECOS() {
        return this.getLoginName();
    }

    @Override
    public long getGID() {
        return JavaPOSIX.LoginInfo.GID;
    }

    @Override
    public String getHome() {
        return System.getProperty("user.home");
    }

    @Override
    public String getLoginName() {
        return System.getProperty("user.name");
    }

    @Override
    public int getPasswdChangeTime() {
        this.handler.unimplementedError("passwd.pw_change unimplemented");
        return 0;
    }

    @Override
    public String getPassword() {
        this.handler.unimplementedError("passwd.pw_passwd unimplemented");
        return null;
    }

    @Override
    public String getShell() {
        this.handler.unimplementedError("passwd.pw_env unimplemented");
        return null;
    }

    @Override
    public long getUID() {
        return JavaPOSIX.LoginInfo.UID;
    }

    @Override
    public int getExpire() {
        this.handler.unimplementedError("passwd.expire unimplemented");
        return -1;
    }
}

