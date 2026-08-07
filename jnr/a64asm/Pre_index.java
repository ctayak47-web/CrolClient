
package jnr.a64asm;

import jnr.a64asm.Immediate;
import jnr.a64asm.Operand;
import jnr.a64asm.Register;

public final class Pre_index
extends Operand {
    private final Immediate preIndex;
    private final Register basereg;

    public Pre_index(Register base, Immediate preIndex) {
        super(12, 0);
        this.basereg = base;
        this.preIndex = preIndex;
    }

    public final Immediate getPreIndex() {
        return this.preIndex;
    }

    public final Register getRegister() {
        return this.basereg;
    }
}

