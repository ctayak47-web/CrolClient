
package com.kenai.jnr.x86asm;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Deprecated
public enum CPU {
    X86_32,
    X86_64;

    public static final CPU I386;

    static {
        I386 = X86_32;
    }
}

