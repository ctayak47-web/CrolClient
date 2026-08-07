
package jnr.posix;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;

public abstract class Flock
extends Struct {
    public Flock(Runtime runtime) {
        super(runtime);
    }

    public abstract void type(short var1);

    public abstract void whence(short var1);

    public abstract void start(long var1);

    public abstract void len(long var1);

    public abstract void pid(int var1);

    public abstract short type();

    public abstract short whence();

    public abstract long start();

    public abstract long len();

    public abstract int pid();
}

