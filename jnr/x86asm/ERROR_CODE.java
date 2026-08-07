
package jnr.x86asm;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum ERROR_CODE {
    ERROR_NONE,
    ERROR_NO_HEAP_MEMORY,
    ERROR_NO_VIRTUAL_MEMORY,
    ERROR_UNKNOWN_INSTRUCTION,
    ERROR_ILLEGAL_INSTRUCTION,
    ERROR_ILLEGAL_ADDRESING,
    ERROR_ILLEGAL_SHORT_JUMP,
    _ERROR_COUNT;

    public final int intValue() {
        return this.ordinal();
    }
}

