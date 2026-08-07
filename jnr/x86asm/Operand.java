
package jnr.x86asm;

import jnr.x86asm.BaseReg;

public class Operand {
    private final int op;
    private final int size;

    public Operand(int op, int size) {
        this.op = op;
        this.size = size;
    }

    public int op() {
        return this.op;
    }

    public int size() {
        return this.size;
    }

    public boolean isNone() {
        return this.op() == 0;
    }

    public boolean isReg() {
        return this.op() == 1;
    }

    public boolean isMem() {
        return this.op() == 2;
    }

    public boolean isImm() {
        return this.op() == 3;
    }

    public boolean isLabel() {
        return this.op() == 4;
    }

    public final boolean isRegMem() {
        return this.isMem() || this.isReg();
    }

    public final boolean isRegCode(int code) {
        return this instanceof BaseReg && ((BaseReg)this).code() == code;
    }

    public final boolean isRegType(int type) {
        return this instanceof BaseReg && ((BaseReg)this).type() == type;
    }

    public final boolean isRegIndex(int index) {
        return this instanceof BaseReg && ((BaseReg)this).index() == index;
    }

    public final boolean isRegMem(int regType) {
        return this.isMem() || this.isRegType(regType);
    }
}

