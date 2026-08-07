
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.posix.Timespec;

public final class DefaultNativeTimespec
extends Timespec {
    public final Struct.SignedLong tv_sec = new Struct.SignedLong();
    public final Struct.SignedLong tv_nsec = new Struct.SignedLong();

    public DefaultNativeTimespec(Runtime runtime) {
        super(runtime);
    }

    @Override
    public void setTime(long[] timespec) {
        assert (timespec.length == 2);
        this.tv_sec.set(timespec[0]);
        this.tv_nsec.set(timespec[1]);
    }

    @Override
    public void sec(long sec) {
        this.tv_sec.set(sec);
    }

    @Override
    public void nsec(long usec) {
        this.tv_nsec.set(usec);
    }

    @Override
    public long sec() {
        return this.tv_sec.get();
    }

    @Override
    public long nsec() {
        return this.tv_nsec.get();
    }
}

