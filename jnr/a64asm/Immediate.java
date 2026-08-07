
package jnr.a64asm;

import jnr.a64asm.Operand;
import jnr.a64asm.RELOC_MODE;

public final class Immediate
extends Operand {
    private final long value;
    private final boolean isUnsigned;
    private final RELOC_MODE relocMode;

    public Immediate(long value, boolean isUnsigned) {
        super(3, 0);
        this.value = value;
        this.isUnsigned = isUnsigned;
        this.relocMode = RELOC_MODE.RELOC_NONE;
    }

    public long value() {
        return this.value;
    }

    public final byte byteValue() {
        return (byte)this.value;
    }

    public final short shortValue() {
        return (short)this.value;
    }

    public final int intValue() {
        return (int)this.value;
    }

    public final long longValue() {
        return this.value;
    }

    public final boolean isUnsigned() {
        return this.isUnsigned;
    }

    RELOC_MODE relocMode() {
        return this.relocMode;
    }

    public static final Immediate imm(long value) {
        return new Immediate(value, false);
    }

    public static final Immediate uimm(long value) {
        return new Immediate(value, true);
    }
}

