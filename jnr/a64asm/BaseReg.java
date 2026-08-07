
package jnr.a64asm;

import jnr.a64asm.Operand;

public abstract class BaseReg
extends Operand {
    public final int code;

    public BaseReg(int code, int size) {
        super(1, size);
        this.code = code;
    }

    public final int type() {
        return this.code() & 0xF0;
    }

    public final int code() {
        return this.code;
    }

    public final int index() {
        return this.code() & 0xF;
    }
}

