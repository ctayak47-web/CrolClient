
package com.kenai.jnr.x86asm;

import com.kenai.jnr.x86asm.CONDITION;
import com.kenai.jnr.x86asm.HINT;
import com.kenai.jnr.x86asm.INST_CODE;
import com.kenai.jnr.x86asm.Immediate;
import com.kenai.jnr.x86asm.Label;
import com.kenai.jnr.x86asm.Operand;

@Deprecated
public abstract class SerializerCore {
    static final Operand _none = new Operand(0, 0){};
    static INST_CODE[] _jcctable = new INST_CODE[]{INST_CODE.INST_JO, INST_CODE.INST_JNO, INST_CODE.INST_JB, INST_CODE.INST_JAE, INST_CODE.INST_JE, INST_CODE.INST_JNE, INST_CODE.INST_JBE, INST_CODE.INST_JA, INST_CODE.INST_JS, INST_CODE.INST_JNS, INST_CODE.INST_JPE, INST_CODE.INST_JPO, INST_CODE.INST_JL, INST_CODE.INST_JGE, INST_CODE.INST_JLE, INST_CODE.INST_JG};
    static INST_CODE[] _cmovcctable = new INST_CODE[]{INST_CODE.INST_CMOVO, INST_CODE.INST_CMOVNO, INST_CODE.INST_CMOVB, INST_CODE.INST_CMOVAE, INST_CODE.INST_CMOVE, INST_CODE.INST_CMOVNE, INST_CODE.INST_CMOVBE, INST_CODE.INST_CMOVA, INST_CODE.INST_CMOVS, INST_CODE.INST_CMOVNS, INST_CODE.INST_CMOVPE, INST_CODE.INST_CMOVPO, INST_CODE.INST_CMOVL, INST_CODE.INST_CMOVGE, INST_CODE.INST_CMOVLE, INST_CODE.INST_CMOVG};
    static final INST_CODE[] _setcctable = new INST_CODE[]{INST_CODE.INST_SETO, INST_CODE.INST_SETNO, INST_CODE.INST_SETB, INST_CODE.INST_SETAE, INST_CODE.INST_SETE, INST_CODE.INST_SETNE, INST_CODE.INST_SETBE, INST_CODE.INST_SETA, INST_CODE.INST_SETS, INST_CODE.INST_SETNS, INST_CODE.INST_SETPE, INST_CODE.INST_SETPO, INST_CODE.INST_SETL, INST_CODE.INST_SETGE, INST_CODE.INST_SETLE, INST_CODE.INST_SETG};

    abstract void _emitX86(INST_CODE var1, Operand var2, Operand var3, Operand var4);

    void emitX86(INST_CODE code) {
        this._emitX86(code, _none, _none, _none);
    }

    void emitX86(INST_CODE code, Operand o1) {
        this._emitX86(code, o1, _none, _none);
    }

    void emitX86(INST_CODE code, Operand o1, Operand o2) {
        this._emitX86(code, o1, o2, _none);
    }

    void emitX86(INST_CODE code, Operand o1, Operand o2, Operand o3) {
        this._emitX86(code, o1, o2, o3);
    }

    void _emitJcc(INST_CODE code, Label label, int hint) {
        if (hint == 0) {
            this.emitX86(code, label);
        } else {
            this.emitX86(code, label, Immediate.imm(hint));
        }
    }

    void _emitJcc(INST_CODE code, Label label, HINT hint) {
        if (hint == HINT.HINT_NONE) {
            this.emitX86(code, label);
        } else {
            this.emitX86(code, label, Immediate.imm(hint.value()));
        }
    }

    abstract boolean is64();

    static INST_CODE conditionToJCC(CONDITION cc) {
        assert (cc.value() <= 15);
        return _jcctable[cc.value()];
    }

    static INST_CODE conditionToCMovCC(CONDITION cc) {
        assert (cc.value() <= 15);
        return _cmovcctable[cc.value()];
    }

    static INST_CODE conditionToSetCC(CONDITION cc) {
        assert (cc.value() <= 15);
        return _setcctable[cc.value()];
    }
}

