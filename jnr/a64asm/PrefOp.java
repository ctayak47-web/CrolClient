
package jnr.a64asm;

import jnr.a64asm.Operand;
import jnr.a64asm.PREF_ENUM;

public class PrefOp
extends Operand {
    PREF_ENUM type;

    PrefOp(long type, PREF_ENUM value) {
        super(11, 0);
        this.type = value;
    }

    public PREF_ENUM type() {
        return this.type;
    }
}

