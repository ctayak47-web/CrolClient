
package com.kenai.jnr.x86asm;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
public enum SEGMENT {
    SEGMENT_NONE(0),
    SEGMENT_CS(46),
    SEGMENT_SS(54),
    SEGMENT_DS(62),
    SEGMENT_ES(38),
    SEGMENT_FS(100),
    SEGMENT_GS(100);

    private final int prefix;

    private SEGMENT(int prefix) {
        this.prefix = prefix;
    }

    public final int prefix() {
        return this.prefix;
    }
}

