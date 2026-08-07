
package jnr.a64asm;

import jnr.a64asm.Immediate;
import jnr.a64asm.Operand;
import jnr.a64asm.Register;

public final class Offset
extends Operand {
    private final Immediate offset;
    private final Register basereg;

    public Offset(Register base, Immediate offset) {
        super(14, 0);
        this.offset = offset;
        this.basereg = base;
    }

    public final Immediate getOffset() {
        return this.offset;
    }

    public final Register getRegister() {
        return this.basereg;
    }
}

