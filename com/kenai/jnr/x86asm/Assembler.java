
package com.kenai.jnr.x86asm;

import com.kenai.jnr.x86asm.BaseReg;
import com.kenai.jnr.x86asm.CPU;
import com.kenai.jnr.x86asm.CodeBuffer;
import com.kenai.jnr.x86asm.CpuInfo;
import com.kenai.jnr.x86asm.HINT;
import com.kenai.jnr.x86asm.INST_CODE;
import com.kenai.jnr.x86asm.Immediate;
import com.kenai.jnr.x86asm.InstructionDescription;
import com.kenai.jnr.x86asm.InstructionGroup;
import com.kenai.jnr.x86asm.Label;
import com.kenai.jnr.x86asm.LinkData;
import com.kenai.jnr.x86asm.Logger;
import com.kenai.jnr.x86asm.MMRegister;
import com.kenai.jnr.x86asm.Mem;
import com.kenai.jnr.x86asm.Operand;
import com.kenai.jnr.x86asm.RELOC_MODE;
import com.kenai.jnr.x86asm.Register;
import com.kenai.jnr.x86asm.RelocData;
import com.kenai.jnr.x86asm.SEGMENT;
import com.kenai.jnr.x86asm.Serializer;
import com.kenai.jnr.x86asm.TrampolineWriter;
import com.kenai.jnr.x86asm.Util;
import com.kenai.jnr.x86asm.X87Register;
import com.kenai.jnr.x86asm.XMMRegister;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

@Deprecated
public final class Assembler
extends Serializer {
    private final CodeBuffer _buffer = new CodeBuffer();
    private final List<RelocData> _relocData = new LinkedList<RelocData>();
    private final CpuInfo cpuInfo = CpuInfo.GENERIC;
    private int _properties = 0;
    int _trampolineSize;
    private final Logger _logger = null;
    private final CPU cpu;
    public static final CPU I386 = CPU.I386;
    public static final CPU X86_64 = CPU.X86_64;
    private static final int[] nop1 = new int[]{144};
    private static final int[] nop2 = new int[]{102, 144};
    private static final int[] nop3 = new int[]{15, 31, 0};
    private static final int[] nop4 = new int[]{15, 31, 64, 0};
    private static final int[] nop5 = new int[]{15, 31, 68, 0, 0};
    private static final int[] nop6 = new int[]{102, 15, 31, 68, 0, 0};
    private static final int[] nop7 = new int[]{15, 31, 128, 0, 0, 0, 0};
    private static final int[] nop8 = new int[]{15, 31, 132, 0, 0, 0, 0, 0};
    private static final int[] nop9 = new int[]{102, 15, 31, 132, 0, 0, 0, 0, 0};
    private static final int[] nop10 = new int[]{102, 102, 15, 31, 132, 0, 0, 0, 0, 0};
    private static final int[] nop11 = new int[]{102, 102, 102, 15, 31, 132, 0, 0, 0, 0, 0};

    boolean is64() {
        return this.cpu == CPU.X86_64;
    }

    private static final int intValue(boolean b) {
        return b ? 1 : 0;
    }

    public Assembler(CPU cpu) {
        this.cpu = cpu;
    }

    public final int offset() {
        return this._buffer.offset();
    }

    public final int codeSize() {
        return this._buffer.offset() + this.trampolineSize();
    }

    int trampolineSize() {
        return this._trampolineSize;
    }

    public final byte getByteAt(int pos) {
        return this._buffer.getByteAt(pos);
    }

    public final short getWordAt(int pos) {
        return this._buffer.getWordAt(pos);
    }

    public final int getDWordAt(int pos) {
        return this._buffer.getDWordAt(pos);
    }

    public final long getQWordAt(int pos) {
        return this._buffer.getQWordAt(pos);
    }

    public final void setByteAt(int pos, byte x) {
        this._buffer.setByteAt(pos, x);
    }

    public final void setWordAt(int pos, short x) {
        this._buffer.setWordAt(pos, x);
    }

    public final void setDWordAt(int pos, int x) {
        this._buffer.setDWordAt(pos, x);
    }

    public final void setQWordAt(int pos, long x) {
        this._buffer.setQWordAt(pos, x);
    }

    public final int getInt32At(int pos) {
        return this._buffer.getDWordAt(pos);
    }

    public final void setInt32At(int pos, long x) {
        this._buffer.setDWordAt(pos, (int)x);
    }

    public final void setVarAt(int pos, long i, boolean isUnsigned, int size) {
        switch (size) {
            case 1: {
                this.setByteAt(pos, (byte)i);
                break;
            }
            case 2: {
                this.setWordAt(pos, (short)i);
                break;
            }
            case 4: {
                this.setDWordAt(pos, (int)i);
                break;
            }
            case 8: {
                this.setQWordAt(pos, i);
            }
            default: {
                throw new IllegalArgumentException("invalid size");
            }
        }
    }

    final void _emitByte(int x) {
        this._buffer.emitByte((byte)x);
    }

    final void _emitWord(int x) {
        this._buffer.emitWord((short)x);
    }

    final void _emitDWord(int x) {
        this._buffer.emitDWord(x);
    }

    final void _emitQWord(long x) {
        this._buffer.emitQWord(x);
    }

    final void _emitInt32(int x) {
        this._buffer.emitDWord(x);
    }

    final void _emitSysInt(long x) {
        if (this.is64()) {
            this._buffer.emitQWord(x);
        } else {
            this._buffer.emitDWord((int)x);
        }
    }

    final void _emitOpCode(int opCode) {
        if ((opCode & 0xFF000000) != 0) {
            this._emitByte((byte)((opCode & 0xFF000000) >> 24));
        }
        if ((opCode & 0xFF0000) != 0) {
            this._emitByte((byte)((opCode & 0xFF0000) >> 16));
        }
        if ((opCode & 0xFF00) != 0) {
            this._emitByte((byte)((opCode & 0xFF00) >> 8));
        }
        this._emitByte((byte)(opCode & 0xFF));
    }

    void _emitSegmentPrefix(Operand rm) {
        SEGMENT segmentPrefix;
        if (rm.isMem() && (segmentPrefix = ((Mem)rm).segmentPrefix()) != SEGMENT.SEGMENT_NONE) {
            this._emitByte(segmentPrefix.prefix());
        }
    }

    void _emitImmediate(Immediate imm, int size) {
        switch (size) {
            case 1: {
                this._emitByte(imm.byteValue());
                break;
            }
            case 2: {
                this._emitWord(imm.shortValue());
                break;
            }
            case 4: {
                this._emitDWord(imm.intValue());
                break;
            }
            case 8: {
                if (!this.is64()) {
                    throw new IllegalArgumentException("64 bit immediate values not supported for 32bit");
                }
                this._emitQWord(imm.longValue());
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid immediate operand size");
            }
        }
    }

    void _emitRexR(int w, int opReg, int regCode) {
        if (this.is64()) {
            boolean b;
            boolean r = (opReg & 8) != 0;
            boolean bl = b = (regCode & 8) != 0;
            if (w != 0 || r || b || (this._properties & 2) != 0) {
                this._emitByte(0x40 | w << 3 | Assembler.intValue(r) << 2 | Assembler.intValue(b));
            }
        }
    }

    void _emitRexR(boolean w, int opReg, int regCode) {
        this._emitRexR(Assembler.intValue(w), opReg, regCode);
    }

    void _emitRexRM(int w, int opReg, Operand rm) {
        if (this.is64()) {
            boolean r = (opReg & 8) != 0;
            boolean x = false;
            boolean b = false;
            if (rm.isReg()) {
                b = (((BaseReg)rm).code() & 8) != 0;
            } else if (rm.isMem()) {
                x = (((Mem)rm).index() & 8) != 0 && ((Mem)rm).index() != 255;
                boolean bl = b = (((Mem)rm).base() & 8) != 0 && ((Mem)rm).base() != 255;
            }
            if (w != 0 || r || x || b || (this._properties & 2) != 0) {
                this._emitByte(0x40 | w << 3 | Assembler.intValue(r) << 2 | Assembler.intValue(x) << 1 | Assembler.intValue(b));
            }
        }
    }

    void _emitRexRM(boolean w, int opReg, Operand rm) {
        this._emitRexRM(Assembler.intValue(w), opReg, rm);
    }

    void _emitModM(int opReg, Mem mem, int immSize) {
        assert (mem.op() == 2);
        int baseReg = mem.base() & 7;
        int indexReg = mem.index() & 7;
        long disp = mem.displacement();
        int shift = mem.shift();
        if (mem.hasBase() && !mem.hasIndex()) {
            if (baseReg == 4) {
                int mod = 0;
                if (disp != 0L) {
                    mod = Util.isInt8(disp) ? 1 : 2;
                }
                this._emitMod(mod, opReg, 4);
                this._emitSib(0, 4, 4);
                if (disp != 0L) {
                    if (Util.isInt8(disp)) {
                        this._emitByte((byte)disp);
                    } else {
                        this._emitInt32((int)disp);
                    }
                }
            } else if (baseReg != 5 && disp == 0L) {
                this._emitMod(0, opReg, baseReg);
            } else if (Util.isInt8(disp)) {
                this._emitMod(1, opReg, baseReg);
                this._emitByte((byte)disp);
            } else {
                this._emitMod(2, opReg, baseReg);
                this._emitInt32((int)disp);
            }
        } else if (mem.hasBase() && mem.hasIndex()) {
            if (baseReg != 5 && disp == 0L) {
                this._emitMod(0, opReg, 4);
                this._emitSib(shift, indexReg, baseReg);
            } else if (Util.isInt8(disp)) {
                this._emitMod(1, opReg, 4);
                this._emitSib(shift, indexReg, baseReg);
                this._emitByte((byte)disp);
            } else {
                this._emitMod(2, opReg, 4);
                this._emitSib(shift, indexReg, baseReg);
                this._emitInt32((int)disp);
            }
        } else if (!this.is64()) {
            if (mem.hasIndex()) {
                this._emitMod(0, opReg, 4);
                this._emitSib(shift, indexReg, 5);
            } else {
                this._emitMod(0, opReg, 5);
            }
            if (mem.hasLabel()) {
                Label label = mem.label();
                int relocId = this._relocData.size();
                long destination = disp;
                if (label.isBound()) {
                    destination += (long)label.position();
                    this._emitInt32(0);
                } else {
                    this._emitDisplacement((Label)label, (long)((long)(-4 - immSize)), (int)4).relocId = relocId;
                }
                RelocData rd = new RelocData(RelocData.Type.RELATIVE_TO_ABSOLUTE, 4, this.offset(), destination);
                this._relocData.add(rd);
            } else {
                this._emitInt32((int)(mem.target() + disp));
            }
        } else if (mem.hasLabel()) {
            Label label = mem.label();
            if (mem.hasIndex()) {
                throw new IllegalArgumentException("illegal addressing");
            }
            this._emitMod(0, opReg, 5);
            disp -= (long)(4 + immSize);
            if (label.isBound()) {
                this._emitInt32((int)(disp += (long)(this.offset() - label.position())));
            } else {
                this._emitDisplacement(label, disp, 4);
            }
        } else {
            this._emitMod(0, opReg, 4);
            if (mem.hasIndex()) {
                this._emitSib(shift, indexReg, 5);
            } else {
                this._emitSib(0, 4, 5);
            }
            long target = mem.target() + disp;
            if (target > 0xFFFFFFFFL) {
                this._logger.log("; Warning: Absolute address truncated to 32 bits\n");
            }
            this._emitInt32((int)target);
        }
    }

    void _emitX86Inl(int opCode, boolean i16bit, boolean rexw, int reg) {
        this._emitX86Inl(opCode, i16bit, Assembler.intValue(rexw), reg);
    }

    void _emitX86Inl(int opCode, boolean i16bit, int rexw, int reg) {
        if (i16bit) {
            this._emitByte(102);
        }
        if ((opCode & 0xFF000000) != 0) {
            this._emitByte((opCode & 0xFF000000) >> 24);
        }
        if (this.is64()) {
            this._emitRexR(rexw, 0, reg);
        }
        if ((opCode & 0xFF0000) != 0) {
            this._emitByte((opCode & 0xFF0000) >> 16);
        }
        if ((opCode & 0xFF00) != 0) {
            this._emitByte((opCode & 0xFF00) >> 8);
        }
        this._emitByte((opCode & 0xFF) + (reg & 7));
    }

    void _emitModRM(int opReg, Operand op, int immSize) {
        assert (op.op() == 1 || op.op() == 2);
        if (op.op() == 1) {
            this._emitModR(opReg, ((BaseReg)op).code());
        } else {
            this._emitModM(opReg, (Mem)op, immSize);
        }
    }

    void _emitMod(int m, int o, int r) {
        this._emitByte((byte)((m & 3) << 6 | (o & 7) << 3 | r & 7));
    }

    void _emitSib(int s, int i, int b) {
        this._emitByte((byte)((s & 3) << 6 | (i & 7) << 3 | b & 7));
    }

    void _emitModR(int opReg, int r) {
        this._emitMod(3, opReg, r);
    }

    void _emitModR(int opReg, BaseReg r) {
        this._emitMod(3, opReg, r.code());
    }

    void _emitX86RM(int opCode, boolean i16bit, boolean rexw, int o, Operand op, int immSize) {
        this._emitX86RM(opCode, i16bit, Assembler.intValue(rexw), o, op, immSize);
    }

    void _emitX86RM(int opCode, boolean i16bit, int rexw, int o, Operand op, int immSize) {
        if (i16bit) {
            this._emitByte(102);
        }
        this._emitSegmentPrefix(op);
        if ((opCode & 0xFF000000) != 0) {
            this._emitByte((opCode & 0xFF000000) >> 24);
        }
        if (this.is64()) {
            this._emitRexRM(rexw, o, op);
        }
        if ((opCode & 0xFF0000) != 0) {
            this._emitByte((byte)((opCode & 0xFF0000) >> 16));
        }
        if ((opCode & 0xFF00) != 0) {
            this._emitByte((byte)((opCode & 0xFF00) >> 8));
        }
        this._emitByte((byte)(opCode & 0xFF));
        this._emitModRM(o, op, immSize);
    }

    void _emitX86(INST_CODE code, Operand o1, Operand o2, Operand o3) {
        InstructionDescription id = InstructionDescription.find(code);
        switch (id.group) {
            case I_EMIT: {
                this._emitOpCode(id.opCode1);
                return;
            }
            case I_ALU: {
                int opCode = id.opCode1;
                int opReg = id.opCodeR;
                if (o1.isMem() && o2.isReg()) {
                    this._emitX86RM(opCode + Assembler.intValue(!o2.isRegType(0)), o2.isRegType(16), o2.isRegType(48), ((Register)o2).code(), o1, 0);
                    return;
                }
                if (o1.isReg() && o2.isRegMem()) {
                    this._emitX86RM(opCode + 2 + Assembler.intValue(!o1.isRegType(0)), o1.isRegType(16), o1.isRegType(48), ((Register)o1).code(), o2, 0);
                    return;
                }
                if (o1.isRegIndex(0) && o2.isImm()) {
                    if (o1.isRegType(16)) {
                        this._emitByte(102);
                    } else if (o1.isRegType(48)) {
                        this._emitByte(72);
                    }
                    this._emitByte(opReg << 3 | 4 + Assembler.intValue(!o1.isRegType(0)));
                    this._emitImmediate((Immediate)o2, o1.size() <= 4 ? o1.size() : 4);
                    return;
                }
                if (!o1.isRegMem() || !o2.isImm()) break;
                Immediate imm = (Immediate)o2;
                int immSize = Util.isInt8(imm.value()) ? 1 : (o1.size() <= 4 ? o1.size() : 4);
                this._emitX86RM(id.opCode2 + (o1.size() != 1 ? (immSize != 1 ? 1 : 3) : 0), o1.size() == 2, o1.size() == 8, opReg, o1, immSize);
                this._emitImmediate((Immediate)o2, immSize);
                return;
            }
            case I_BSWAP: {
                if (!o1.isReg()) break;
                Register dst = (Register)o1;
                if (this.is64()) {
                    this._emitRexR(dst.type() == 48, 1, dst.code());
                }
                this._emitByte(15);
                this._emitModR(1, dst.code());
                return;
            }
            case I_BT: {
                if (o1.isRegMem() && o2.isReg()) {
                    Operand dst = o1;
                    Register src = (Register)o2;
                    this._emitX86RM(id.opCode1, src.isRegType(16), src.isRegType(48), src.code(), dst, 0);
                    return;
                }
                if (!o1.isRegMem() || !o2.isImm()) break;
                Operand dst = o1;
                Immediate src = (Immediate)o2;
                this._emitX86RM(id.opCode2, src.size() == 2, src.size() == 8, id.opCodeR, dst, 1);
                this._emitImmediate(src, 1);
                return;
            }
            case I_CALL: {
                if (o1.isRegMem(this.is64() ? 48 : 32)) {
                    Operand dst = o1;
                    this._emitX86RM(255, false, false, 2, dst, 0);
                    return;
                }
                if (o1.isImm()) {
                    Immediate imm = (Immediate)o1;
                    this._emitByte(232);
                    this._emitJmpOrCallReloc(InstructionGroup.I_CALL, imm.value());
                    return;
                }
                if (!o1.isLabel()) break;
                Label label = (Label)o1;
                if (label.isBound()) {
                    int rel32_size = 5;
                    int offs = label.position() - this.offset();
                    assert (offs <= 0);
                    this._emitByte(232);
                    this._emitInt32(offs - 5);
                } else {
                    this._emitByte(232);
                    this._emitDisplacement(label, -4L, 4);
                }
                return;
            }
            case I_CRC32: {
                if (!o1.isReg() || !o2.isRegMem()) break;
                Register dst = (Register)o1;
                Operand src = o2;
                assert (dst.type() == 32 || dst.type() == 48);
                this._emitX86RM(id.opCode1 + Assembler.intValue(src.size() != 1), src.size() == 2, dst.type() == 8, dst.code(), src, 0);
                return;
            }
            case I_ENTER: {
                if (!o1.isImm() || !o2.isImm()) break;
                this._emitByte(200);
                this._emitImmediate((Immediate)o1, 2);
                this._emitImmediate((Immediate)o2, 1);
                break;
            }
            case I_IMUL: {
                if (o1.isRegMem() && o2.isNone() && o3.isNone()) {
                    Operand src = o1;
                    this._emitX86RM(246 + Assembler.intValue(src.size() != 1), src.size() == 2, src.size() == 8, 5, src, 0);
                    return;
                }
                if (o1.isReg() && !o2.isNone() && o3.isNone()) {
                    Register dst = (Register)o1;
                    assert (!dst.isRegType(16));
                    if (o2.isRegMem()) {
                        Operand src = o2;
                        this._emitX86RM(4015, dst.isRegType(16), dst.isRegType(48), dst.code(), src, 0);
                        return;
                    }
                    if (!o2.isImm()) break;
                    Immediate imm = (Immediate)o2;
                    if (Util.isInt8(imm.value()) && imm.relocMode() == RELOC_MODE.RELOC_NONE) {
                        this._emitX86RM(107, dst.isRegType(16), dst.isRegType(48), dst.code(), (Operand)dst, 1);
                        this._emitImmediate(imm, 1);
                    } else {
                        int immSize = dst.isRegType(16) ? 2 : 4;
                        this._emitX86RM(105, dst.isRegType(16), dst.isRegType(48), dst.code(), (Operand)dst, immSize);
                        this._emitImmediate(imm, immSize);
                    }
                    return;
                }
                if (!o1.isReg() || !o2.isRegMem() || !o3.isImm()) break;
                Register dst = (Register)o1;
                Operand src = o2;
                Immediate imm = (Immediate)o3;
                if (Util.isInt8(imm.value()) && imm.relocMode() == RELOC_MODE.RELOC_NONE) {
                    this._emitX86RM(107, dst.isRegType(16), dst.isRegType(48), dst.code(), src, 1);
                    this._emitImmediate(imm, 1);
                } else {
                    int immSize = dst.isRegType(16) ? 2 : 4;
                    this._emitX86RM(105, dst.isRegType(16), dst.isRegType(48), dst.code(), src, immSize);
                    this._emitImmediate(imm, immSize);
                }
                return;
            }
            case I_INC_DEC: {
                if (!o1.isRegMem()) break;
                Operand dst = o1;
                if (!this.is64() && dst.isReg() && (dst.isRegType(16) || dst.isRegType(32))) {
                    this._emitX86Inl(id.opCode1, dst.isRegType(16), 0, ((BaseReg)dst).code());
                    return;
                }
                this._emitX86RM(id.opCode2 + Assembler.intValue(dst.size() != 1), dst.size() == 2, dst.size() == 8, id.opCodeR, dst, 0);
                return;
            }
            case I_J: {
                HINT hint;
                if (!o1.isLabel()) break;
                Label label = (Label)o1;
                boolean isShortJump = code.ordinal() >= INST_CODE.INST_J_SHORT.ordinal() && code.ordinal() <= INST_CODE.INST_JMP_SHORT.ordinal();
                HINT hINT = hint = o2.isImm() ? HINT.valueOf((int)((Immediate)o2).value()) : HINT.HINT_NONE;
                if (hint == HINT.HINT_TAKEN || hint == HINT.HINT_NOT_TAKEN && (this._properties & 4) != 0) {
                    this._emitByte(hint.value());
                }
                if (label.isBound()) {
                    int rel8_size = 2;
                    int rel32_size = 6;
                    int offs = label.position() - this.offset();
                    assert (offs <= 0);
                    if (Util.isInt8(offs - 2)) {
                        this._emitByte(0x70 | id.opCode1 & 0xFF);
                        this._emitByte((byte)(offs - 2));
                    } else {
                        if (isShortJump && this._logger != null) {
                            this._logger.log("; WARNING: Emitting long conditional jump, but short jump instruction forced!");
                        }
                        this._emitByte(15);
                        this._emitByte(0x80 | id.opCode1 & 0xFF);
                        this._emitInt32(offs - 6);
                    }
                } else if (isShortJump) {
                    this._emitByte(0x70 | id.opCode1 & 0xFF);
                    this._emitDisplacement(label, -1L, 1);
                } else {
                    this._emitByte(15);
                    this._emitByte(0x80 | id.opCode1 & 0xFF);
                    this._emitDisplacement(label, -4L, 4);
                }
                return;
            }
            case I_JMP: {
                boolean isShortJump;
                if (o1.isRegMem()) {
                    Operand dst = o1;
                    this._emitX86RM(255, false, false, 4, dst, 0);
                    return;
                }
                if (o1.isImm()) {
                    Immediate imm = (Immediate)o1;
                    this._emitByte(233);
                    this._emitJmpOrCallReloc(InstructionGroup.I_JMP, imm.value());
                    return;
                }
                if (!o1.isLabel()) break;
                Label label = (Label)o1;
                boolean bl = isShortJump = code == INST_CODE.INST_JMP_SHORT;
                if (label.isBound()) {
                    int rel8_size = 2;
                    int rel32_size = 5;
                    int offs = label.position() - this.offset();
                    if (Util.isInt8(offs - 2)) {
                        this._emitByte(235);
                        this._emitByte((byte)(offs - 2));
                    } else {
                        if (isShortJump && this._logger != null) {
                            this._logger.log("; WARNING: Emitting long jump, but short jump instruction forced!");
                        }
                        this._emitByte(233);
                        this._emitInt32(offs - 5);
                    }
                } else if (isShortJump) {
                    this._emitByte(235);
                    this._emitDisplacement(label, -1L, 1);
                } else {
                    this._emitByte(233);
                    this._emitDisplacement(label, -4L, 4);
                }
                return;
            }
            case I_LEA: {
                if (!o1.isReg() || !o2.isMem()) break;
                Register dst = (Register)o1;
                Mem src = (Mem)o2;
                this._emitX86RM(141, dst.isRegType(16), dst.isRegType(48), dst.code(), (Operand)src, 0);
                return;
            }
            case I_M: {
                if (!o1.isMem()) break;
                this._emitX86RM(id.opCode1, false, (byte)id.opCode2, id.opCodeR, (Operand)((Mem)o1), 0);
                return;
            }
            case I_MOV: {
                Operand dst = o1;
                Operand src = o2;
                switch (dst.op() << 4 | src.op()) {
                    case 17: {
                        assert (src.isRegType(0) || src.isRegType(16) || src.isRegType(32) || src.isRegType(48));
                    }
                    case 18: {
                        assert (dst.isRegType(0) || dst.isRegType(16) || dst.isRegType(32) || dst.isRegType(48));
                        this._emitX86RM(138 + Assembler.intValue(!dst.isRegType(0)), dst.isRegType(16), dst.isRegType(48), ((Register)dst).code(), src, 0);
                        return;
                    }
                    case 19: {
                        Immediate isrc = (Immediate)o2;
                        int immSize = dst.size();
                        if (this.is64() && immSize == 8 && Util.isInt32(isrc.value()) && isrc.relocMode() == RELOC_MODE.RELOC_NONE) {
                            this._emitX86RM(199, dst.isRegType(16), dst.isRegType(48), 0, dst, 0);
                            immSize = 4;
                        } else {
                            this._emitX86Inl(dst.size() == 1 ? 176 : 184, dst.isRegType(16), dst.isRegType(48), ((Register)dst).code());
                        }
                        this._emitImmediate(isrc, immSize);
                        return;
                    }
                    case 33: {
                        assert (src.isRegType(0) || src.isRegType(16) || src.isRegType(32) || src.isRegType(48));
                        this._emitX86RM(136 + Assembler.intValue(!src.isRegType(0)), src.isRegType(16), src.isRegType(48), ((Register)src).code(), dst, 0);
                        return;
                    }
                    case 35: {
                        int immSize = dst.size() <= 4 ? dst.size() : 4;
                        this._emitX86RM(198 + Assembler.intValue(dst.size() != 1), dst.size() == 2, dst.size() == 8, 0, dst, immSize);
                        this._emitImmediate((Immediate)src, immSize);
                        return;
                    }
                }
                break;
            }
            case I_MOV_PTR: {
                if ((!o1.isReg() || !o2.isImm()) && (!o1.isImm() || !o2.isReg())) break;
                boolean reverse = o1.op() == 1;
                int opCode = !reverse ? 160 : 162;
                Register reg = (Register)(!reverse ? o1 : o2);
                Immediate imm = (Immediate)(!reverse ? o2 : o1);
                if (reg.index() != 0) {
                    throw new IllegalStateException("reg.index() != 0");
                }
                if (reg.isRegType(16)) {
                    this._emitByte(102);
                }
                if (this.is64()) {
                    this._emitRexR(reg.size() == 8, 0, 0);
                }
                this._emitByte(opCode + Assembler.intValue(reg.size() != 1));
                this._emitImmediate(imm, this.is64() ? 8 : 4);
                return;
            }
            case I_MOVSX_MOVZX: {
                if (!o1.isReg() || !o2.isRegMem()) break;
                Register dst = (Register)o1;
                Operand src = o2;
                if (dst.isRegType(0)) {
                    throw new IllegalArgumentException("not gpb");
                }
                if (src.size() != 1 && src.size() != 2) {
                    throw new IllegalArgumentException("src.size !=1 && src.size != 2");
                }
                if (src.size() == 2 && dst.isRegType(16)) {
                    throw new IllegalArgumentException("not gpw");
                }
                this._emitX86RM(id.opCode1 + Assembler.intValue(src.size() != 1), dst.isRegType(16), dst.isRegType(48), dst.code(), src, 0);
                return;
            }
            case I_MOVSXD: {
                if (!this.is64()) {
                    throw new IllegalStateException("illegal instruction");
                }
                if (!o1.isReg() || !o2.isRegMem()) break;
                Register dst = (Register)o1;
                Operand src = o2;
                this._emitX86RM(99, false, 1, dst.code(), src, 0);
                return;
            }
            case I_PUSH: {
                if (o1.isImm()) {
                    Immediate imm = (Immediate)o1;
                    if (Util.isInt8(imm.value()) && imm.relocMode() == RELOC_MODE.RELOC_NONE) {
                        this._emitByte(106);
                        this._emitImmediate(imm, 1);
                    } else {
                        this._emitByte(104);
                        this._emitImmediate(imm, 4);
                    }
                    return;
                }
            }
            case I_POP: {
                if (o1.isReg()) {
                    assert (o1.isRegType(16) || o1.isRegType(this.is64() ? 48 : 32));
                    this._emitX86Inl(id.opCode1, o1.isRegType(16), 0, ((Register)o1).code());
                    return;
                }
                if (!o1.isMem()) break;
                this._emitX86RM(id.opCode2, o1.size() == 2, 0, id.opCodeR, o1, 0);
                return;
            }
            case I_R_RM: {
                if (!o1.isReg() || !o2.isRegMem()) break;
                Register dst = (Register)o1;
                assert (dst.type() != 0);
                Operand src = o2;
                this._emitX86RM(id.opCode1, dst.type() == 16, dst.type() == 48, dst.code(), src, 0);
                return;
            }
            case I_RM_B: {
                if (!o1.isRegMem()) break;
                Operand op = o1;
                this._emitX86RM(id.opCode1, false, false, 0, op, 0);
                return;
            }
            case I_RM: {
                if (!o1.isRegMem()) break;
                Operand op = o1;
                this._emitX86RM(id.opCode1 + Assembler.intValue(op.size() != 1), op.size() == 2, op.size() == 8, id.opCodeR, op, 0);
                return;
            }
            case I_RM_R: {
                if (!o1.isRegMem() || !o2.isReg()) break;
                Operand dst = o1;
                Register src = (Register)o2;
                this._emitX86RM(id.opCode1 + Assembler.intValue(src.type() != 0), src.type() == 16, src.type() == 48, src.code(), dst, 0);
                return;
            }
            case I_RET: {
                if (o1.isNone()) {
                    this._emitByte(195);
                    return;
                }
                if (!o1.isImm()) break;
                Immediate imm = (Immediate)o1;
                assert (Util.isUInt16(imm.value()));
                if (imm.value() == 0L && imm.relocMode() == RELOC_MODE.RELOC_NONE) {
                    this._emitByte(195);
                } else {
                    this._emitByte(194);
                    this._emitImmediate(imm, 2);
                }
                return;
            }
            case I_ROT: {
                int opCode;
                if (!o1.isRegMem() || !o2.isRegCode(1) && !o2.isImm()) break;
                boolean useImm8 = o2.isImm() && (((Immediate)o2).value() != 1L || ((Immediate)o2).relocMode() != RELOC_MODE.RELOC_NONE);
                int n = opCode = useImm8 ? 192 : 208;
                if (o1.size() != 1) {
                    opCode |= 1;
                }
                if (o2.op() == 1) {
                    opCode |= 2;
                }
                this._emitX86RM(opCode, o1.size() == 2, o1.size() == 8, id.opCodeR, o1, Assembler.intValue(useImm8));
                if (useImm8) {
                    this._emitImmediate((Immediate)o2, 1);
                }
                return;
            }
            case I_SHLD_SHRD: {
                if (!o1.isRegMem() || !o2.isReg() || !o3.isImm() && (!o3.isReg() || !o3.isRegCode(1))) break;
                Operand dst = o1;
                Register src1 = (Register)o2;
                Operand src2 = o3;
                assert (dst.size() == src1.size());
                this._emitX86RM(id.opCode1 + Assembler.intValue(src2.isReg()), src1.isRegType(16), src1.isRegType(48), src1.code(), dst, Assembler.intValue(src2.isImm()));
                if (src2.isImm()) {
                    this._emitImmediate((Immediate)src2, 1);
                }
                return;
            }
            case I_TEST: {
                int immSize;
                if (o1.isRegMem() && o2.isReg()) {
                    assert (o1.size() == o2.size());
                    this._emitX86RM(132 + Assembler.intValue(o2.size() != 1), o2.size() == 2, o2.size() == 8, ((BaseReg)o2).code(), o1, 0);
                    return;
                }
                if (o1.isRegIndex(0) && o2.isImm()) {
                    int immSize2;
                    int n = immSize2 = o1.size() <= 4 ? o1.size() : 4;
                    if (o1.size() == 2) {
                        this._emitByte(102);
                    }
                    if (this.is64()) {
                        this._emitRexRM(o1.size() == 8, 0, o1);
                    }
                    this._emitByte(168 + Assembler.intValue(o1.size() != 1));
                    this._emitImmediate((Immediate)o2, immSize2);
                    return;
                }
                if (!o1.isRegMem() || !o2.isImm()) break;
                int n = immSize = o1.size() <= 4 ? o1.size() : 4;
                if (o1.size() == 2) {
                    this._emitByte(102);
                }
                this._emitSegmentPrefix(o1);
                if (this.is64()) {
                    this._emitRexRM(o1.size() == 8, 0, o1);
                }
                this._emitByte(246 + Assembler.intValue(o1.size() != 1));
                this._emitModRM(0, o1, immSize);
                this._emitImmediate((Immediate)o2, immSize);
                return;
            }
            case I_XCHG: {
                if (!o1.isRegMem() || !o2.isReg()) break;
                Operand dst = o1;
                Register src = (Register)o2;
                if (src.isRegType(16)) {
                    this._emitByte(102);
                }
                this._emitSegmentPrefix(dst);
                if (this.is64()) {
                    this._emitRexRM(src.isRegType(48), src.code(), dst);
                }
                if (dst.op() == 1 && dst.size() > 1 && (((Register)dst).code() == 0 || src.code() == 0)) {
                    int index = ((Register)dst).code() | src.code();
                    this._emitByte((byte)(144 + index));
                    return;
                }
                this._emitByte(134 + Assembler.intValue(!src.isRegType(0)));
                this._emitModRM(src.code(), dst, 0);
                return;
            }
            case I_MOVBE: {
                if (o1.isReg() && o2.isMem()) {
                    this._emitX86RM(997616, o1.isRegType(16), o1.isRegType(48), ((Register)o1).code(), (Operand)((Mem)o2), 0);
                    return;
                }
                if (!o1.isMem() || !o2.isReg()) break;
                this._emitX86RM(997617, o2.isRegType(16), o2.isRegType(48), ((Register)o2).code(), (Operand)((Mem)o1), 0);
                return;
            }
            case I_X87_FPU: {
                if (o1.isRegType(80)) {
                    int i1 = ((X87Register)o1).index();
                    int i2 = 0;
                    if (code != INST_CODE.INST_FCOM && code != INST_CODE.INST_FCOMP) {
                        if (!o2.isRegType(80)) {
                            throw new IllegalArgumentException("not x87 reg");
                        }
                        i2 = ((X87Register)o2).index();
                    } else if (i1 != 0 && i2 != 0) {
                        throw new IllegalArgumentException("illegal instruction");
                    }
                    this._emitByte(i1 == 0 ? (id.opCode1 & 0xFF000000) >> 24 : (id.opCode1 & 0xFF0000) >> 16);
                    this._emitByte(i1 == 0 ? ((id.opCode1 & 0xFF00) >> 8) + i2 : (id.opCode1 & 0xFF) + i1);
                    return;
                }
                if (!o1.isMem() || o1.size() != 4 && o1.size() != 8 || !o2.isNone()) break;
                Mem m = (Mem)o1;
                this._emitSegmentPrefix(m);
                this._emitByte(o1.size() == 4 ? (id.opCode1 & 0xFF000000) >> 24 : (id.opCode1 & 0xFF0000) >> 16);
                this._emitModM(id.opCodeR, m, 0);
                return;
            }
            case I_X87_STI: {
                if (!o1.isRegType(80)) break;
                int i = ((X87Register)o1).index();
                this._emitByte((id.opCode1 & 0xFF00) >> 8);
                this._emitByte((id.opCode1 & 0xFF) + i);
                return;
            }
            case I_X87_FSTSW: {
                if (o1.isReg() && ((BaseReg)o1).type() <= 48 && ((BaseReg)o1).index() == 0) {
                    this._emitOpCode(id.opCode2);
                    return;
                }
                if (!o1.isMem()) break;
                this._emitX86RM(id.opCode1, false, 0, id.opCodeR, (Operand)((Mem)o1), 0);
                return;
            }
            case I_X87_MEM_STI: {
                if (o1.isRegType(80)) {
                    this._emitByte((id.opCode2 & 0xFF000000) >> 24);
                    this._emitByte(((id.opCode2 & 0xFF0000) >> 16) + ((X87Register)o1).index());
                    return;
                }
            }
            case I_X87_MEM: {
                if (!o1.isMem()) {
                    throw new IllegalArgumentException("not x87 mem");
                }
                Mem m = (Mem)o1;
                int opCode = 0;
                int mod = 0;
                if (o1.size() == 2 && (id.o1Flags & 2) != 0) {
                    opCode = (id.opCode1 & 0xFF000000) >> 24;
                    mod = id.opCodeR;
                }
                if (o1.size() == 4 && (id.o1Flags & 4) != 0) {
                    opCode = (id.opCode1 & 0xFF0000) >> 16;
                    mod = id.opCodeR;
                }
                if (o1.size() == 8 && (id.o1Flags & 8) != 0) {
                    opCode = (id.opCode1 & 0xFF00) >> 8;
                    mod = id.opCode1 & 0xFF;
                }
                if (opCode == 0) break;
                this._emitSegmentPrefix(m);
                this._emitByte(opCode);
                this._emitModM(mod, m, 0);
                return;
            }
            case I_MMU_MOV: {
                int rexw;
                assert (id.o1Flags != 0);
                assert (id.o2Flags != 0);
                if (o1.isMem() && (id.o1Flags & 0x40) == 0 || o1.isRegType(96) && (id.o1Flags & 0x10) == 0 || o1.isRegType(112) && (id.o1Flags & 0x20) == 0 || o1.isRegType(32) && (id.o1Flags & 4) == 0 || o1.isRegType(48) && (id.o1Flags & 8) == 0 || o2.isRegType(96) && (id.o2Flags & 0x10) == 0 || o2.isRegType(112) && (id.o2Flags & 0x20) == 0 || o2.isRegType(32) && (id.o2Flags & 4) == 0 || o2.isRegType(48) && (id.o2Flags & 8) == 0 || o2.isMem() && (id.o2Flags & 0x40) == 0) {
                    throw new IllegalArgumentException("illegal instruction");
                }
                if (o1.isMem() && o2.isMem()) {
                    throw new IllegalArgumentException("illegal instruction");
                }
                int n = ((id.o1Flags | id.o2Flags) & 1) != 0 ? 0 : (rexw = Assembler.intValue(o1.isRegType(48) || o1.isRegType(48)));
                if (o1.isReg() && o2.isReg()) {
                    this._emitMmu(id.opCode1, rexw, ((BaseReg)o1).code(), (BaseReg)o2, 0);
                    return;
                }
                if (o1.isReg() && o2.isMem()) {
                    this._emitMmu(id.opCode1, rexw, ((BaseReg)o1).code(), (Mem)o2, 0);
                    return;
                }
                if (!o1.isMem() || !o2.isReg()) break;
                this._emitMmu(id.opCode2, rexw, ((BaseReg)o2).code(), (Mem)o1, 0);
                return;
            }
            case I_MMU_MOVD: {
                if ((o1.isRegType(96) || o1.isRegType(112)) && (o2.isRegType(32) || o2.isMem())) {
                    this._emitMmu(o1.isRegType(112) ? 1711279982 : 3950, 0, ((BaseReg)o1).code(), o2, 0);
                    return;
                }
                if (!o1.isRegType(32) && !o1.isMem() || !o2.isRegType(96) && !o2.isRegType(112)) break;
                this._emitMmu(o2.isRegType(112) ? 1711279998 : 3966, 0, ((BaseReg)o2).code(), o1, 0);
                return;
            }
            case I_MMU_MOVQ: {
                if (o1.isRegType(96) && o2.isRegType(96)) {
                    this._emitMmu(3951, 0, ((MMRegister)o1).code(), (MMRegister)o2, 0);
                    return;
                }
                if (o1.isRegType(112) && o2.isRegType(112)) {
                    this._emitMmu(-218099842, 0, ((XMMRegister)o1).code(), (XMMRegister)o2, 0);
                    return;
                }
                if (o1.isRegType(96) && o2.isRegType(112)) {
                    this._emitMmu(-234876970, 0, ((MMRegister)o1).code(), (XMMRegister)o2, 0);
                    return;
                }
                if (o1.isRegType(112) && o2.isRegType(96)) {
                    this._emitMmu(-218099754, 0, ((XMMRegister)o1).code(), (MMRegister)o2, 0);
                    return;
                }
                if (o1.isRegType(96) && o2.isMem()) {
                    this._emitMmu(3951, 0, ((MMRegister)o1).code(), (Mem)o2, 0);
                    return;
                }
                if (o1.isRegType(112) && o2.isMem()) {
                    this._emitMmu(-218099842, 0, ((XMMRegister)o1).code(), (Mem)o2, 0);
                    return;
                }
                if (o1.isMem() && o2.isRegType(96)) {
                    this._emitMmu(3967, 0, ((MMRegister)o2).code(), (Mem)o1, 0);
                    return;
                }
                if (o1.isMem() && o2.isRegType(112)) {
                    this._emitMmu(1711280086, 0, ((XMMRegister)o2).code(), (Mem)o1, 0);
                    return;
                }
                if (!this.is64()) break;
                if ((o1.isRegType(96) || o1.isRegType(112)) && (o2.isRegType(48) || o2.isMem())) {
                    this._emitMmu(o1.isRegType(112) ? 1711279982 : 3950, 1, ((BaseReg)o1).code(), o2, 0);
                    return;
                }
                if (!o1.isRegType(48) && !o1.isMem() || !o2.isRegType(96) && !o2.isRegType(112)) break;
                this._emitMmu(o2.isRegType(112) ? 1711279998 : 3966, 1, ((BaseReg)o2).code(), o1, 0);
                return;
            }
            case I_MMU_PREFETCH: {
                if (!o1.isMem() || !o2.isImm()) break;
                Mem mem = (Mem)o1;
                Immediate hint = (Immediate)o2;
                this._emitMmu(3864, 0, (int)hint.value(), mem, 0);
                return;
            }
            case I_MMU_PEXTR: {
                boolean isGpdGpq;
                if (!(o1.isRegMem() && (o2.isRegType(112) || code == INST_CODE.INST_PEXTRW && o2.isRegType(96)) && o3.isImm())) {
                    throw new IllegalStateException("illegal instruction");
                }
                int opCode = id.opCode1;
                boolean bl = isGpdGpq = o1.isRegType(32) || o1.isRegType(48);
                if (code == INST_CODE.INST_PEXTRB && o1.size() != 0 && o1.size() != 1 && !isGpdGpq) {
                    throw new IllegalStateException("illegal instruction");
                }
                if (code == INST_CODE.INST_PEXTRW && o1.size() != 0 && o1.size() != 2 && !isGpdGpq) {
                    throw new IllegalStateException("illegal instruction");
                }
                if (code == INST_CODE.INST_PEXTRD && o1.size() != 0 && o1.size() != 4 && !isGpdGpq) {
                    throw new IllegalStateException("illegal instruction");
                }
                if (code == INST_CODE.INST_PEXTRQ && o1.size() != 0 && o1.size() != 8 && !isGpdGpq) {
                    throw new IllegalStateException("illegal instruction");
                }
                if (o2.isRegType(112)) {
                    opCode |= 0x66000000;
                }
                if (o1.isReg()) {
                    this._emitMmu(opCode, id.opCodeR | Assembler.intValue(o1.isRegType(48)), ((BaseReg)o2).code(), (BaseReg)o1, 1);
                    this._emitImmediate((Immediate)o3, 1);
                    return;
                }
                if (!o1.isMem()) break;
                this._emitMmu(opCode, id.opCodeR, ((BaseReg)o2).code(), (Mem)o1, 1);
                this._emitImmediate((Immediate)o3, 1);
                return;
            }
            case I_MMU_RMI: {
                int rexw;
                int prefix;
                assert (id.o1Flags != 0);
                assert (id.o2Flags != 0);
                if (!o1.isReg() || o1.isRegType(96) && (id.o1Flags & 0x10) == 0 || o1.isRegType(112) && (id.o1Flags & 0x20) == 0 || o1.isRegType(32) && (id.o1Flags & 4) == 0 || o1.isRegType(48) && (id.o1Flags & 8) == 0 || o2.isRegType(96) && (id.o2Flags & 0x10) == 0 || o2.isRegType(112) && (id.o2Flags & 0x20) == 0 || o2.isRegType(32) && (id.o2Flags & 4) == 0 || o2.isRegType(48) && (id.o2Flags & 8) == 0 || o2.isMem() && (id.o2Flags & 0x40) == 0 || o2.isImm() && (id.o2Flags & 0x80) == 0) {
                    throw new IllegalStateException("illegal instruction");
                }
                int n = prefix = (id.o1Flags & 0x30) == 48 && o1.isRegType(112) || (id.o2Flags & 0x30) == 48 && o2.isRegType(112) ? 0x66000000 : 0;
                int n2 = ((id.o1Flags | id.o2Flags) & 1) != 0 ? 0 : (rexw = Assembler.intValue(o1.isRegType(48) || o1.isRegType(48)));
                if (o2.isReg()) {
                    if ((id.o2Flags & 0x3C) == 0) {
                        throw new IllegalStateException("illegal instruction");
                    }
                    this._emitMmu(id.opCode1 | prefix, rexw, ((BaseReg)o1).code(), (BaseReg)o2, 0);
                    return;
                }
                if (o2.isMem()) {
                    if ((id.o2Flags & 0x40) == 0) {
                        throw new IllegalStateException("illegal instruction");
                    }
                    this._emitMmu(id.opCode1 | prefix, rexw, ((BaseReg)o1).code(), (Mem)o2, 0);
                    return;
                }
                if (!o2.isImm()) break;
                if ((id.o2Flags & 0x80) == 0) {
                    throw new IllegalStateException("illegal instruction");
                }
                this._emitMmu(id.opCode2 | prefix, rexw, id.opCodeR, (BaseReg)o1, 1);
                this._emitImmediate((Immediate)o2, 1);
                return;
            }
            case I_MMU_RM_IMM8: {
                int rexw;
                int prefix;
                assert (id.o1Flags != 0);
                assert (id.o2Flags != 0);
                if (!o1.isReg() || o1.isRegType(96) && (id.o1Flags & 0x10) == 0 || o1.isRegType(112) && (id.o1Flags & 0x20) == 0 || o1.isRegType(32) && (id.o1Flags & 4) == 0 || o1.isRegType(48) && (id.o1Flags & 8) == 0 || o2.isRegType(96) && (id.o2Flags & 0x10) == 0 || o2.isRegType(112) && (id.o2Flags & 0x20) == 0 || o2.isRegType(32) && (id.o2Flags & 4) == 0 || o2.isRegType(48) && (id.o2Flags & 8) == 0 || o2.isMem() && (id.o2Flags & 0x40) == 0 || !o3.isImm()) {
                    throw new IllegalStateException("illegal instruction");
                }
                int n = prefix = (id.o1Flags & 0x30) == 48 && o1.isRegType(112) || (id.o2Flags & 0x30) == 48 && o2.isRegType(112) ? 0x66000000 : 0;
                int n3 = ((id.o1Flags | id.o2Flags) & 1) != 0 ? 0 : (rexw = Assembler.intValue(o1.isRegType(48) || o1.isRegType(48)));
                if (o2.isReg()) {
                    if ((id.o2Flags & 0x3C) == 0) {
                        throw new IllegalStateException("illegal instruction");
                    }
                    this._emitMmu(id.opCode1 | prefix, rexw, ((BaseReg)o1).code(), (BaseReg)o2, 1);
                    this._emitImmediate((Immediate)o3, 1);
                    return;
                }
                if (!o2.isMem()) break;
                if ((id.o2Flags & 0x40) == 0) {
                    throw new IllegalStateException("illegal instruction");
                }
                this._emitMmu(id.opCode1 | prefix, rexw, ((BaseReg)o1).code(), (Mem)o2, 1);
                this._emitImmediate((Immediate)o3, 1);
                return;
            }
            case I_MMU_RM_3DNOW: {
                if (!o1.isRegType(96) || !o2.isRegType(96) && !o2.isMem()) break;
                this._emitMmu(id.opCode1, 0, ((BaseReg)o1).code(), (Mem)o2, 1);
                this._emitByte(id.opCode2);
                return;
            }
        }
    }

    void _emitFpu(int opCode) {
        this._emitOpCode(opCode);
    }

    void _emitFpuSTI(int opCode, int sti) {
        assert (0 <= sti && sti < 8);
        this._emitOpCode(opCode + sti);
    }

    void _emitFpuMEM(int opCode, int opReg, Mem mem) {
        this._emitSegmentPrefix(mem);
        if ((opCode & 0xFF000000) != 0) {
            this._emitByte((opCode & 0xFF000000) >> 24);
        }
        if (this.is64()) {
            this._emitRexRM(0, opReg, (Operand)mem);
        }
        if ((opCode & 0xFF0000) != 0) {
            this._emitByte((opCode & 0xFF0000) >> 16);
        }
        if ((opCode & 0xFF00) != 0) {
            this._emitByte((opCode & 0xFF00) >> 8);
        }
        this._emitByte(opCode & 0xFF);
        this._emitModM(opReg, mem, 0);
    }

    void _emitMmu(int opCode, int rexw, int opReg, Operand src, int immSize) {
        this._emitSegmentPrefix(src);
        if ((opCode & 0xFF000000) != 0) {
            this._emitByte((opCode & 0xFF000000) >> 24);
        }
        if (this.is64()) {
            this._emitRexRM(rexw, opReg, src);
        }
        if ((opCode & 0xFF0000) != 0) {
            this._emitByte((opCode & 0xFF0000) >> 16);
        }
        this._emitByte((opCode & 0xFF00) >> 8);
        this._emitByte(opCode & 0xFF);
        if (src.isReg()) {
            this._emitModR(opReg, ((BaseReg)src).code());
        } else {
            this._emitModM(opReg, (Mem)src, immSize);
        }
    }

    LinkData _emitDisplacement(Label label, long inlinedDisplacement, int size) {
        assert (!label.isBound());
        assert (size == 1 || size == 4);
        LinkData link = new LinkData(this.offset(), inlinedDisplacement, -1);
        label.link(link);
        if (size == 1) {
            this._emitByte(1);
        } else {
            this._emitDWord(0x4040404);
        }
        return link;
    }

    void _emitJmpOrCallReloc(InstructionGroup instruction, long target) {
        if (this.is64()) {
            this._trampolineSize += 14;
        }
        RelocData rd = new RelocData(RelocData.Type.ABSOLUTE_TO_RELATIVE_TRAMPOLINE, 4, this.offset(), target);
        this._relocData.add(rd);
        this._emitInt32(0);
    }

    public void relocCode(ByteBuffer buffer, long address) {
        int csize = this.codeSize();
        this._buffer.copyTo(buffer);
        for (RelocData r : this._relocData) {
            long val;
            boolean useTrampoline = false;
            assert (r.offset + r.size <= csize);
            switch (r.type) {
                case ABSOLUTE_TO_ABSOLUTE: {
                    val = r.destination;
                    break;
                }
                case RELATIVE_TO_ABSOLUTE: {
                    val = address + r.destination;
                    break;
                }
                case ABSOLUTE_TO_RELATIVE: 
                case ABSOLUTE_TO_RELATIVE_TRAMPOLINE: {
                    val = r.destination - (address + (long)r.offset + 4L);
                    if (!this.is64() || r.type != RelocData.Type.ABSOLUTE_TO_RELATIVE_TRAMPOLINE || Util.isInt32(val)) break;
                    val = (long)buffer.position() - (long)(r.offset + 4);
                    useTrampoline = true;
                    break;
                }
                default: {
                    throw new IllegalStateException("invalid relocation type");
                }
            }
            switch (r.size) {
                case 4: {
                    buffer.putInt(r.offset, (int)val);
                    break;
                }
                case 8: {
                    buffer.putLong(r.offset, val);
                    break;
                }
                default: {
                    throw new IllegalStateException("invalid relocation size");
                }
            }
            if (!this.is64() || !useTrampoline) continue;
            if (this._logger != null) {
                this._logger.log(String.format("; Trampoline from %x -> %x\n", address + (long)r.offset, r.destination));
            }
            TrampolineWriter.writeTrampoline(buffer, r.destination);
        }
    }

    public void align(long m) {
        if (this._logger != null) {
            this._logger.logAlign(m);
        }
        if (m < 1L) {
            return;
        }
        if (m > 64L) {
            assert (m <= 64L);
            return;
        }
        int i = (int)(m - (long)this.offset() % m);
        if ((long)i == m) {
            return;
        }
        if ((this._properties & 1) != 0) {
            if (this.cpuInfo.vendor == CpuInfo.Vendor.INTEL && ((this.cpuInfo.family & 0xF) == 6 || (this.cpuInfo.family & 0xF) == 15)) {
                do {
                    int n;
                    int[] p;
                    switch (i) {
                        case 1: {
                            p = nop1;
                            n = 1;
                            break;
                        }
                        case 2: {
                            p = nop2;
                            n = 2;
                            break;
                        }
                        case 3: {
                            p = nop3;
                            n = 3;
                            break;
                        }
                        case 4: {
                            p = nop4;
                            n = 4;
                            break;
                        }
                        case 5: {
                            p = nop5;
                            n = 5;
                            break;
                        }
                        case 6: {
                            p = nop6;
                            n = 6;
                            break;
                        }
                        case 7: {
                            p = nop7;
                            n = 7;
                            break;
                        }
                        case 8: {
                            p = nop8;
                            n = 8;
                            break;
                        }
                        default: {
                            p = nop9;
                            n = 9;
                        }
                    }
                    i -= n;
                    int idx = 0;
                    while (n > 0) {
                        this._emitByte(p[idx]);
                        ++idx;
                        --n;
                    }
                } while (i > 0);
                return;
            }
            if (this.cpuInfo.vendor == CpuInfo.Vendor.AMD && this.cpuInfo.family >= 15) {
                do {
                    int n;
                    int[] p;
                    switch (i) {
                        case 1: {
                            p = nop1;
                            n = 1;
                            break;
                        }
                        case 2: {
                            p = nop2;
                            n = 2;
                            break;
                        }
                        case 3: {
                            p = nop3;
                            n = 3;
                            break;
                        }
                        case 4: {
                            p = nop4;
                            n = 4;
                            break;
                        }
                        case 5: {
                            p = nop5;
                            n = 5;
                            break;
                        }
                        case 6: {
                            p = nop6;
                            n = 6;
                            break;
                        }
                        case 7: {
                            p = nop7;
                            n = 7;
                            break;
                        }
                        case 8: {
                            p = nop8;
                            n = 8;
                            break;
                        }
                        case 9: {
                            p = nop9;
                            n = 9;
                            break;
                        }
                        case 10: {
                            p = nop10;
                            n = 10;
                            break;
                        }
                        default: {
                            p = nop11;
                            n = 11;
                        }
                    }
                    i -= n;
                    int idx = 0;
                    while (n > 0) {
                        this._emitByte(p[idx]);
                        ++idx;
                        --n;
                    }
                } while (i > 0);
                return;
            }
            if (!this.is64()) {
                do {
                    switch (i) {
                        default: {
                            this._emitByte(102);
                            --i;
                        }
                        case 3: {
                            this._emitByte(102);
                            --i;
                        }
                        case 2: {
                            this._emitByte(102);
                            --i;
                        }
                        case 1: 
                    }
                    this._emitByte(144);
                } while (--i > 0);
            }
        }
        while (i-- > 0) {
            this._emitByte(144);
        }
    }
}

