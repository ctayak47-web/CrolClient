
package jnr.unixsocket;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

final class Ucred
extends Struct {
    final Struct.pid_t pid = new Struct.pid_t();
    final Struct.uid_t uid = new Struct.uid_t();
    final Struct.gid_t gid = new Struct.gid_t();

    public Ucred() {
        super(Runtime.getSystemRuntime());
    }

    Struct.pid_t getPidField() {
        return this.pid;
    }

    Struct.uid_t getUidField() {
        return this.uid;
    }

    Struct.gid_t getGidField() {
        return this.gid;
    }
}

