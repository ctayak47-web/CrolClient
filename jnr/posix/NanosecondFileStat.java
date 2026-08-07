
package jnr.posix;

import jnr.posix.FileStat;

public interface NanosecondFileStat
extends FileStat {
    public long aTimeNanoSecs();

    public long cTimeNanoSecs();

    public long mTimeNanoSecs();
}

