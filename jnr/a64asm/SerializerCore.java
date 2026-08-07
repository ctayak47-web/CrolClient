
package jnr.a64asm;

import jnr.a64asm.INST_CODE;
import jnr.a64asm.Operand;

public abstract class SerializerCore {
    static final Operand _none = new Operand(0, 0){};

    abstract void _emita64(INST_CODE var1, Operand var2, Operand var3, Operand var4, Operand var5, Operand var6);

    void emitA64(INST_CODE code) {
        this._emita64(code, _none, _none, _none, _none, _none);
    }

    void emitA64(INST_CODE code, Operand o1) {
        this._emita64(code, o1, _none, _none, _none, _none);
    }

    void emitA64(INST_CODE code, Operand o1, Operand o2) {
        this._emita64(code, o1, o2, _none, _none, _none);
    }

    void emitA64(INST_CODE code, Operand o1, Operand o2, Operand o3) {
        this._emita64(code, o1, o2, o3, _none, _none);
    }

    void emitA64(INST_CODE code, Operand o1, Operand o2, Operand o3, Operand o4) {
        this._emita64(code, o1, o2, o3, o4, _none);
    }

    void emitA64(INST_CODE code, Operand o1, Operand o2, Operand o3, Operand o4, Operand o5) {
        this._emita64(code, o1, o2, o3, o4, o5);
    }

    abstract boolean is64();
}

