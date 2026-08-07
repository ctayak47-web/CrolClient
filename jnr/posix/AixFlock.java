
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.posix.Flock;

public final class AixFlock
extends Flock {
    public final Struct.Signed16 l_type = new Struct.Signed16();
    public final Struct.Signed16 l_whence = new Struct.Signed16();
    public final Struct.Unsigned32 l_sysid = new Struct.Unsigned32();
    public final Struct.Signed32 l_pid = new Struct.Signed32();
    public final Struct.Signed32 l_vfs = new Struct.Signed32();
    public final Struct.SignedLong l_start = new Struct.SignedLong();
    public final Struct.SignedLong l_len = new Struct.SignedLong();

    public AixFlock(Runtime runtime) {
        super(runtime);
    }

    @Override
    public void type(short type) {
        this.l_type.set(type);
    }

    @Override
    public void whence(short whence) {
        this.l_whence.set(whence);
    }

    @Override
    public void start(long start) {
        this.l_start.set(start);
    }

    @Override
    public void len(long len) {
        this.l_len.set(len);
    }

    @Override
    public void pid(int pid) {
        this.l_pid.set(pid);
    }

    @Override
    public short type() {
        return this.l_type.get();
    }

    @Override
    public short whence() {
        return this.l_whence.get();
    }

    @Override
    public long start() {
        return this.l_start.get();
    }

    @Override
    public long len() {
        return this.l_len.get();
    }

    @Override
    public int pid() {
        return this.l_pid.get();
    }
}

