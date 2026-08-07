
package jnr.a64asm;

public enum EXTEND_ENUM {
    UXTB,
    UXTH,
    UXTW,
    LSL,
    UXTX,
    SXTB,
    SXTH,
    SXTW,
    SXTX;

    public final int intValue() {
        return this.ordinal();
    }
}

