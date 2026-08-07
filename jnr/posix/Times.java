
package jnr.posix;

public interface Times {
    public long utime();

    public long stime();

    public long cutime();

    public long cstime();
}

