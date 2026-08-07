
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.posix.Timeval;

public final class DefaultNativeTimeval
extends Timeval {
    public final Struct.SignedLong tv_sec = new Struct.SignedLong();
    public final Struct.SignedLong tv_usec = new Struct.SignedLong();

    public DefaultNativeTimeval(Runtime runtime) {
        super(runtime);
    }

    @Override
    public void setTime(long[] timeval) {
        assert (timeval.length == 2);
        this.tv_sec.set(timeval[0]);
        this.tv_usec.set(timeval[1]);
    }

    @Override
    public void sec(long sec) {
        this.tv_sec.set(sec);
    }

    @Override
    public void usec(long usec) {
        this.tv_usec.set(usec);
    }

    @Override
    public long sec() {
        return this.tv_sec.get();
    }

    @Override
    public long usec() {
        return this.tv_usec.get();
    }
}

