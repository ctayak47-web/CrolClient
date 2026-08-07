
package jnr.posix;

import jnr.posix.Times;

final class JavaTimes
implements Times {
    private static final long startTime = System.currentTimeMillis();
    static final long HZ = 1000L;

    JavaTimes() {
    }

    @Override
    public long utime() {
        return Math.max(System.currentTimeMillis() - startTime, 1L);
    }

    @Override
    public long stime() {
        return 0L;
    }

    @Override
    public long cutime() {
        return 0L;
    }

    @Override
    public long cstime() {
        return 0L;
    }
}

