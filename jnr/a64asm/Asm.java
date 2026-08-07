
package jnr.a64asm;

import jnr.a64asm.CPU_A64;
import jnr.a64asm.Immediate;
import jnr.a64asm.Label;
import jnr.a64asm.Mem;
import jnr.a64asm.Register;

public final class Asm {
    public static final CPU_A64 Aarch_64 = CPU_A64.A64;
    public static final Register no_reg = new Register(255, 0);
    public static final Register x0 = Register.gpr(0);
    public static final Register x1 = Register.gpr(1);
    public static final Register x2 = Register.gpr(2);
    public static final Register x3 = Register.gpr(3);
    public static final Register x4 = Register.gpr(4);
    public static final Register x5 = Register.gpr(5);
    public static final Register x6 = Register.gpr(6);
    public static final Register x7 = Register.gpr(7);
    public static final Register fp = Register.gpr(29);
    public static final Register lr = Register.gpr(30);
    public static final Register sp = Register.gpr(31);
    public static final Register w0 = Register.gpr(32);
    public static final Register w1 = Register.gpr(33);
    public static final Register w2 = Register.gpr(34);
    public static final Register w3 = Register.gpr(35);
    public static final Register w4 = Register.gpr(36);
    public static final Register w5 = Register.gpr(37);
    public static final Register w6 = Register.gpr(38);
    public static final Register w7 = Register.gpr(39);
    public static final Register w8 = Register.gpr(40);
    public static final Register w9 = Register.gpr(41);
    public static final Register w10 = Register.gpr(42);
    public static final Register w11 = Register.gpr(43);
    public static final Register w12 = Register.gpr(44);
    public static final Register w13 = Register.gpr(45);
    public static final Register w14 = Register.gpr(46);
    public static final Register w15 = Register.gpr(47);

    private Asm() {
    }

    static final Mem _ptr_build(Label label, long disp, int ptrSize) {
        return new Mem(label, disp, ptrSize);
    }

    static final Mem _ptr_build(Label label, Register index, int shift, long disp, int ptrSize) {
        return new Mem(label, index, shift, disp, ptrSize);
    }

    static final Mem _ptr_build_abs(long target, long disp, int ptrSize) {
        return new Mem(target, disp, ptrSize);
    }

    static final Mem _ptr_build_abs(long target, Register index, int shift, long disp, int ptrSize) {
        return new Mem(target, index, shift, disp, ptrSize);
    }

    static final Mem _ptr_build(Register base, long disp, int ptrSize) {
        return new Mem(base, disp, ptrSize);
    }

    static final Mem _ptr_build(Register base, Register index, int shift, long disp, int ptrSize) {
        return new Mem(base, index, shift, disp, ptrSize);
    }

    public static final Mem ptr(Label label, long disp) {
        return Asm._ptr_build(label, disp, 0);
    }

    public static final Mem ptr(Label label) {
        return Asm._ptr_build(label, 0L, 0);
    }

    public static final Mem word_ptr(Label label, long disp) {
        return Asm._ptr_build(label, disp, 32);
    }

    public static final Mem word_ptr(Label label) {
        return Asm._ptr_build(label, 0L, 32);
    }

    public static final Mem dword_ptr(Label label, long disp) {
        return Asm._ptr_build(label, disp, 64);
    }

    public static final Mem dword_ptr(Label label) {
        return Asm._ptr_build(label, 0L, 64);
    }

    public static final Mem ptr(Label label, Register index, int shift, long disp) {
        return Asm._ptr_build(label, index, shift, disp, 0);
    }

    public static final Mem word_ptr(Label label, Register index, int shift, long disp) {
        return Asm._ptr_build(label, index, shift, disp, 32);
    }

    public static final Mem dword_ptr(Label label, Register index, int shift, long disp) {
        return Asm._ptr_build(label, index, shift, disp, 64);
    }

    public static final Mem word_ptr_abs(long target, Register index, int shift, long disp) {
        return Asm._ptr_build_abs(target, index, shift, disp, 32);
    }

    public static final Mem dword_ptr_abs(long target, Register index, int shift, long disp) {
        return Asm._ptr_build_abs(target, index, shift, disp, 64);
    }

    public static final Mem ptr(Register base, long disp) {
        return Asm._ptr_build(base, disp, 0);
    }

    public static final Mem word_ptr(Register base, long disp) {
        return Asm._ptr_build(base, disp, 32);
    }

    public static final Mem dword_ptr(Register base, long disp) {
        return Asm._ptr_build(base, disp, 64);
    }

    public static final Mem ptr(Register base, Register index, int shift, long disp) {
        return Asm._ptr_build(base, index, shift, disp, 0);
    }

    public static final Mem word_ptr(Register base, Register index, int shift, long disp) {
        return Asm._ptr_build(base, index, shift, disp, 32);
    }

    public static final Mem dword_ptr(Register base, Register index, int shift, long disp) {
        return Asm._ptr_build(base, index, shift, disp, 64);
    }

    public static final Immediate imm(long value) {
        return Immediate.imm(value);
    }

    public static final Immediate uimm(long value) {
        return Immediate.imm(value);
    }
}

