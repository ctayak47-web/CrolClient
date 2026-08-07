
package jnr.a64asm;

import jnr.a64asm.Operand;

public final class Conditions
extends Operand {
    private final int value;

    public Conditions(int value) {
        super(7, 0);
        this.value = value;
    }

    public long value() {
        return this.value;
    }
}

