
package jnr.a64asm;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import jnr.a64asm.CPU_A64;
import jnr.a64asm.CodeBuffer;
import jnr.a64asm.Conditions;
import jnr.a64asm.CpuInfo;
import jnr.a64asm.Ext;
import jnr.a64asm.INST_CODE;
import jnr.a64asm.Immediate;
import jnr.a64asm.InstructionDescription;
import jnr.a64asm.InstructionGroup;
import jnr.a64asm.Label;
import jnr.a64asm.Logger;
import jnr.a64asm.Offset;
import jnr.a64asm.Operand;
import jnr.a64asm.PRFOP_ENUM;
import jnr.a64asm.Post_index;
import jnr.a64asm.Pre_index;
import jnr.a64asm.Register;
import jnr.a64asm.RelocData;
import jnr.a64asm.Serializer;
import jnr.a64asm.Shift;
import jnr.a64asm.SysRegDescription;
import jnr.a64asm.SysRegister;

public final class Assembler_A64
extends Serializer {
    private final CodeBuffer _buffer = new CodeBuffer();
    private final List<RelocData> _relocData = new LinkedList<RelocData>();
    private final CpuInfo cpuInfo = CpuInfo.GENERIC;
    private int _properties = 0;
    private final Logger _logger = null;
    private final CPU_A64 cpu;
    public static final CPU_A64 Aarch_64 = CPU_A64.Aarch64;

    @Override
    boolean is64() {
        return this.cpu == CPU_A64.A64;
    }

    private static final int intValue(boolean b) {
        return b ? 1 : 0;
    }

    public Assembler_A64(CPU_A64 cpu) {
        this.cpu = cpu;
    }

    public final int offset() {
        return this._buffer.offset();
    }

    public final int codeSize() {
        return this._buffer.offset();
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

    @Override
    void _emita64(INST_CODE code, Operand o1, Operand o2, Operand o3, Operand o4, Operand o5) {
        InstructionDescription id = InstructionDescription.find(code);
        switch (id.group) {
            case addsub_carry: 
            case addsub_ext: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg() && o3.isReg() || o4 != null && o4.isExtend()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Register regM = (Register)o3;
                    Ext extV = null;
                    if (o4 != _none && o4.isExtend()) {
                        extV = (Ext)o4;
                    }
                    if (o1.size() == 64) {
                        inst_to_emit |= Integer.MIN_VALUE;
                    }
                    inst_to_emit |= regD.code & 0x1F;
                    inst_to_emit |= (regN.code & 0x1F) << 5;
                    inst_to_emit |= (regM.code & 0x1F) << 16;
                    if (id.group == InstructionGroup.addsub_ext && extV != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (extV.value() & 7L) << 10);
                        inst_to_emit = (int)((long)inst_to_emit | (extV.type() & 7L) << 13);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case addsub_imm: 
            case addsub_shift: {
                int inst_to_emit = 0;
                if (o1 != _none && o1.isReg() && o2 != _none && o2.isReg()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Immediate val = null;
                    if (o3 != _none && o3.isImm()) {
                        val = (Immediate)o3;
                    }
                    Shift sft = null;
                    if (o4 != _none) {
                        sft = (Shift)o4;
                    }
                    Register regM = null;
                    if (o3 != _none && o3.isReg()) {
                        regM = (Register)o3;
                    }
                    if (o1.size() == 64) {
                        inst_to_emit |= Integer.MIN_VALUE;
                    }
                    inst_to_emit |= regD.code & 0x1F;
                    inst_to_emit |= (regN.code & 0x1F) << 16;
                    if (id.group == InstructionGroup.addsub_shift) {
                        if (regM != null) {
                            inst_to_emit |= (regM.code & 0x1F) << 16;
                        }
                        if (sft != null) {
                            inst_to_emit = (int)((long)inst_to_emit | (sft.value() & 0x3FL) << 10);
                        }
                    } else if (val != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (val.value() & 0xFFFL) << 10);
                    }
                    if (sft != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (sft.type() & 3L) << 22);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case bitfield: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Immediate val1 = null;
                    if (o3.isImm()) {
                        val1 = (Immediate)o3;
                    }
                    Immediate val2 = null;
                    if (o4.isImm()) {
                        val2 = (Immediate)o4;
                    }
                    if (o1.size() == 64) {
                        inst_to_emit |= Integer.MIN_VALUE;
                        inst_to_emit |= 0x400000;
                    }
                    inst_to_emit |= regD.code & 0x1F;
                    inst_to_emit |= (regN.code & 0x1F) << 5;
                    if (val1 != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (val1.value() & 0x3FL) << 10);
                    }
                    if (val2 != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (val2.value() & 0x3FL) << 16);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case branch_imm: {
                boolean inst_to_emit = false;
                if (o1 != _none) {
                    Immediate mem = (Immediate)o1;
                    this._emitJmpOrCallReloc(InstructionGroup.branch_imm, mem.value());
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case branch_reg: {
                int inst_to_emit = 0;
                Register regN = null;
                if (o1 != _none && o1 != null && o1.isReg()) {
                    regN = (Register)o1;
                }
                if (regN != null) {
                    inst_to_emit |= (regN.code & 0x1F) << 5;
                }
                if (regN == null && id.code == INST_CODE.INST_RET_BRANCH_REG) {
                    inst_to_emit |= 0x3C0;
                }
                this._emitInt32(inst_to_emit |= id.opcode);
                break;
            }
            case compbranch: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isLabel()) {
                    Register regD = (Register)o1;
                    Label labl = (Label)o2;
                    if (o1.size() == 64) {
                        inst_to_emit |= Integer.MIN_VALUE;
                    }
                    inst_to_emit |= regD.code & 0x1F;
                    inst_to_emit |= (labl.position() & 0x7FFFF) << 5;
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case condbranch: {
                int inst_to_emit = 0;
                if (o1.isImm()) {
                    Immediate imm19 = (Immediate)o1;
                    if (imm19 != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm19.value() & 0x7FFFFL) << 5);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case condcmp_imm: 
            case condcmp_reg: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isImm()) {
                    Register regD = (Register)o1;
                    Register regM = (Register)o2;
                    Immediate val = (Immediate)o2;
                    Immediate nzcv = (Immediate)o3;
                    Conditions cond = (Conditions)o4;
                    if (regD != null) {
                        inst_to_emit |= regD.code & 0x1F;
                    }
                    if (id.group == InstructionGroup.condcmp_reg && regM != null) {
                        inst_to_emit |= (regM.code & 0x1F) << 16;
                    } else if (val != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (val.value() & 0x1FL) << 16);
                    }
                    if (nzcv != null) {
                        inst_to_emit = (int)((long)inst_to_emit | nzcv.value() & 0xFL);
                    }
                    if (cond != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (cond.value() & 0xFL) << 12);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case condsel: {
                int inst_to_emit = 0;
                if (o1.isReg()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Register regM = (Register)o3;
                    Conditions cond0 = (Conditions)o4;
                    Conditions cond1 = (Conditions)o3;
                    Conditions cond2 = (Conditions)o2;
                    if (regD != null) {
                        inst_to_emit |= regD.code & 0x1F;
                    }
                    if (o4.isCond() && (cond0.value() & 0xEL) != 14L) {
                        inst_to_emit = (int)((long)inst_to_emit | (cond0.value() ^ 1L) << 12);
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                        inst_to_emit |= (regM.code & 0x1F) << 16;
                    } else if (o3.isCond() && (cond1.value() & 0xEL) != 14L) {
                        inst_to_emit = (int)((long)inst_to_emit | (cond1.value() ^ 1L) << 12);
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                        inst_to_emit |= 0x1F0000;
                    } else if (o2.isCond() && (cond2.value() & 0xEL) != 14L) {
                        inst_to_emit = (int)((long)inst_to_emit | (cond1.value() ^ 1L) << 12);
                        inst_to_emit |= 0x3E0;
                        inst_to_emit |= 0x1F0000;
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case dp_1src: 
            case dp_2src: 
            case dp_3src: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Register regM = (Register)o3;
                    Register regA = (Register)o4;
                    if (regD != null) {
                        inst_to_emit |= regD.code & 0x1F;
                    }
                    if (regN != null) {
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                    }
                    if (regM != null && (id.group == InstructionGroup.dp_2src || id.group == InstructionGroup.dp_3src)) {
                        inst_to_emit |= (regM.code & 0x1F) << 16;
                    }
                    if (regA != null && id.group == InstructionGroup.dp_3src) {
                        inst_to_emit |= (regA.code & 0x1F) << 10;
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case exception: {
                int inst_to_emit = 0;
                if (o1.isImm()) {
                    Immediate imm16 = (Immediate)o1;
                    if (imm16 != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm16.value() & 0xFFFFL) << 5);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case extract: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Register regM = (Register)o3;
                    Immediate imm6 = (Immediate)o4;
                    Immediate imm6_1 = (Immediate)o3;
                    if (regD != null) {
                        inst_to_emit |= regD.code & 0x1F;
                    }
                    if (regN != null) {
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                    }
                    if (o3.isReg() && regM != null) {
                        inst_to_emit |= (regM.code & 0x1F) << 16;
                        inst_to_emit = (int)((long)inst_to_emit | (imm6.value() & 0x3FL) << 10);
                    } else if (o3.isImm() && imm6_1 != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm6_1.value() & 0x3FL) << 10);
                        inst_to_emit |= (regN.code & 0x1F) << 16;
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case ldst_imm9: 
            case ldst_pos: {
                int inst_to_emit = 0;
                if (o1.isReg() || id.code == INST_CODE.INST_PRFM_LDST_POS__IMMEDIATE) {
                    Register regD = null;
                    PRFOP_ENUM pfrop = null;
                    if (id.code == INST_CODE.INST_PRFM_LDST_POS__IMMEDIATE && o1 != null) {
                        pfrop = (PRFOP_ENUM)o1;
                    } else if (o1 != _none) {
                        regD = (Register)o1;
                    }
                    Register regN = null;
                    Post_index postindex = null;
                    Pre_index preindex = null;
                    Offset offset = null;
                    Immediate imm9 = null;
                    Immediate imm12 = null;
                    if (o2 != _none && o2.isReg()) {
                        regN = (Register)o2;
                    } else if (o2 != _none && (o2.isPreIndex() || o2.isPostIndex() || o2.isOffset())) {
                        if (o2.isPreIndex()) {
                            preindex = (Pre_index)o2;
                            regN = preindex.getRegister();
                            imm9 = preindex.getPreIndex();
                        } else if (o2.isPostIndex()) {
                            postindex = (Post_index)o2;
                            regN = postindex.getRegister();
                            imm9 = postindex.getPostIndex();
                        } else {
                            offset = (Offset)o2;
                            regN = offset.getRegister();
                            imm12 = offset.getOffset();
                        }
                    }
                    if (o3 != _none && id.group == InstructionGroup.ldst_imm9 && !o2.isPreIndex() && !o2.isPostIndex()) {
                        imm9 = (Immediate)o3;
                    }
                    if (o3 != _none && id.group == InstructionGroup.ldst_pos && !o2.isPreIndex() && !o2.isPostIndex()) {
                        imm12 = (Immediate)o3;
                    }
                    if (o1.size() == 64 && id.code != INST_CODE.INST_PRFM_LDST_POS__IMMEDIATE && id.code != INST_CODE.INST_LDRSW_IMM_OFF && id.code != INST_CODE.INST_LDRH_IMM_OFF && id.code != INST_CODE.INST_LDRSH_IMM_OFF && id.code != INST_CODE.INST_LDRB_IMM_OFF && id.code != INST_CODE.INST_LDRSB_IMM_OFF) {
                        inst_to_emit |= 0x40000000;
                    }
                    if (o1.size() == 32 && id.code != INST_CODE.INST_LDRB_IMM_OFF && (id.code == INST_CODE.INST_LDRSH_IMM_OFF || id.code == INST_CODE.INST_LDRSB_IMM_OFF)) {
                        inst_to_emit |= 0x400000;
                    }
                    if (regD != null) {
                        inst_to_emit |= regD.code & 0x1F;
                    } else if (pfrop != null) {
                        inst_to_emit = (int)((long)inst_to_emit | pfrop.intValue() & 0x1FL);
                    }
                    if (regN != null) {
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                    }
                    if (id.group == InstructionGroup.ldst_imm9) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm9.value() & 0x1FFL) << 12);
                    } else if (id.group == InstructionGroup.ldst_pos && (id.code == INST_CODE.INST_LDRB_IMM_OFF || id.code == INST_CODE.INST_LDRSB_IMM_OFF)) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm12.value() & 0xFFFL) << 10);
                    } else if (id.group == InstructionGroup.ldst_pos && (id.code == INST_CODE.INST_LDRH_IMM_OFF || id.code == INST_CODE.INST_LDRSH_IMM_OFF)) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm12.value() >> 1 & 0xFFFL) << 10);
                    } else if (id.group == InstructionGroup.ldst_pos) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm12.value() >> (id.code == INST_CODE.INST_LDRSW_IMM_OFF ? 2 : 3) & 0xFFFL) << 10);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case ldst_imm9_2reg: 
            case ldst_pos_2reg: {
                int inst_to_emit = 0;
                if (o1.isReg()) {
                    Register regT = (Register)o1;
                    Register regT2 = (Register)o2;
                    Post_index postindex = null;
                    Pre_index preindex = null;
                    Offset offset = null;
                    Immediate imm7 = null;
                    Register regN = null;
                    if (o3 != _none && o3.isPreIndex() || o3.isPostIndex() || o3.isOffset()) {
                        if (o3.isPreIndex()) {
                            preindex = (Pre_index)o3;
                            regN = preindex.getRegister();
                            imm7 = preindex.getPreIndex();
                        } else if (o3.isPostIndex()) {
                            postindex = (Post_index)o3;
                            regN = postindex.getRegister();
                            imm7 = postindex.getPostIndex();
                        } else {
                            offset = (Offset)o3;
                            regN = offset.getRegister();
                            imm7 = offset.getOffset();
                        }
                    }
                    if (o1.size() == 64) {
                        inst_to_emit |= Integer.MIN_VALUE;
                    }
                    if (regT != null) {
                        inst_to_emit |= regT.code & 0x1F;
                    }
                    if (regN != null) {
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                    }
                    if (regT2 != null) {
                        inst_to_emit |= (regT2.code & 0x1F) << 10;
                    }
                    if (imm7 != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm7.value() >> (o1.size() == 64 ? 3 : 2) & 0x7FL) << 15);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case ldst_regoff: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg() || id.code == INST_CODE.INST_PRFM_LDST_REGOFF__REGISTER) {
                    Register regD = null;
                    PRFOP_ENUM pfrop = null;
                    if (id.code == INST_CODE.INST_PRFM_LDST_REGOFF__REGISTER) {
                        pfrop = (PRFOP_ENUM)o1;
                    } else {
                        regD = (Register)o1;
                    }
                    Register regN = (Register)o2;
                    Register regM = (Register)o3;
                    Ext extnd = (Ext)o3;
                    if (o1.size() == 64 && id.code != INST_CODE.INST_PRFM_LDST_REGOFF__REGISTER) {
                        inst_to_emit |= 0x40000000;
                    }
                    inst_to_emit = regD != null ? (inst_to_emit |= regD.code & 0x1F) : (int)((long)inst_to_emit | pfrop.intValue() & 0x1FL);
                    if (regN != null) {
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                    }
                    if (regM != null) {
                        inst_to_emit |= (regM.code & 0x1F) << 16;
                    }
                    if (o4 != null && o4.isExtend()) {
                        inst_to_emit |= extnd.value() == 3L || extnd.value() == 2L ? 4096 : 0;
                        inst_to_emit = (int)((long)inst_to_emit | (extnd.type() & 7L) << 13);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case ldst_unpriv: 
            case ldst_unscaled: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg() || id.code == INST_CODE.INST_PRFUM_LDST_UNSCALED) {
                    Register regD = null;
                    PRFOP_ENUM pfrop = null;
                    if (id.code == INST_CODE.INST_PRFUM_LDST_UNSCALED) {
                        pfrop = (PRFOP_ENUM)o1;
                    } else {
                        regD = (Register)o1;
                    }
                    Register regN = (Register)o2;
                    Immediate imm9 = (Immediate)o3;
                    inst_to_emit = regD != null ? (inst_to_emit |= regD.code & 0x1F) : (int)((long)inst_to_emit | pfrop.intValue() & 0x1FL);
                    if (regN != null) {
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                    }
                    if (o3 != null && o3.isImm()) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm9.value() & 0x1FFL) << 12);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case ldstexcl: 
            case ldstexcl_op3: {
                int inst_to_emit = 0;
                Register regD = (Register)o1;
                Register regN = (Register)o2;
                Register regM = null;
                Offset offset = null;
                if (o3 != null && o3.isReg()) {
                    regM = (Register)o3;
                } else if (o3 != null && o3.isOffset()) {
                    offset = (Offset)o3;
                    regM = offset.getRegister();
                }
                inst_to_emit |= (regD.code & 0x1F) << 16;
                inst_to_emit |= regN.code & 0x1F;
                if (id.group == InstructionGroup.ldstexcl_op3 && regM != null) {
                    inst_to_emit |= (regM.code & 0x1F) << 5;
                }
                this._emitInt32(inst_to_emit |= id.opcode);
                break;
            }
            case ldstexcl_op4: {
                int inst_to_emit = 0;
                Register regS = (Register)o1;
                Register regt = (Register)o2;
                Register regt2 = (Register)o3;
                Register regN = (Register)o3;
                inst_to_emit |= (regS.code & 0x1F) << 16;
                inst_to_emit |= regt.code & 0x1F;
                inst_to_emit |= (regt2.code & 0x1F) << 10;
                inst_to_emit |= (regN.code & 0x1F) << 5;
                this._emitInt32(inst_to_emit |= id.opcode);
                break;
            }
            case ldstnapair_offs: 
            case ldstpair_off: 
            case ldstpair_indexed: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Register regM = null;
                    Pre_index preindex = null;
                    Post_index posindex = null;
                    Immediate imm7 = null;
                    if (o3.isReg()) {
                        regM = (Register)o3;
                    } else if (o3.isPostIndex()) {
                        posindex = (Post_index)o3;
                        regM = posindex.getRegister();
                        imm7 = posindex.getPostIndex();
                    } else if (o3.isPreIndex()) {
                        preindex = (Pre_index)o3;
                        regM = preindex.getRegister();
                        imm7 = preindex.getPreIndex();
                    }
                    if (o4 != _none && o4.isImm()) {
                        imm7 = (Immediate)o4;
                    }
                    if (id.group == InstructionGroup.ldstexcl || id.group == InstructionGroup.ldstnapair_offs) {
                        if (o1.size() == 64) {
                            inst_to_emit |= 0x40000000;
                        } else if (o1.size() == 128) {
                            inst_to_emit |= Integer.MIN_VALUE;
                        }
                    } else if (o1.size() == 64) {
                        inst_to_emit |= Integer.MIN_VALUE;
                    }
                    inst_to_emit |= regD.code & 0x1F;
                    inst_to_emit |= (regN.code & 0x1F) << 10;
                    inst_to_emit |= (regM.code & 0x1F) << 5;
                    if (imm7 != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (imm7.value() >> (o1.size() == 64 ? 3 : 2) & 0x7FL) << 15);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case loadlit: {
                int inst_to_emit = 0;
                if (o1.isReg() || id.code == INST_CODE.INST_PRFM_LOADLIT__LITERAL) {
                    Register regD = null;
                    PRFOP_ENUM prfop = null;
                    if (id.code == INST_CODE.INST_PRFM_LOADLIT__LITERAL) {
                        prfop = (PRFOP_ENUM)o1;
                    } else {
                        regD = (Register)o1;
                    }
                    Immediate lbl = (Immediate)o2;
                    if (o1.size() == 64 && id.code != INST_CODE.INST_PRFM_LOADLIT__LITERAL) {
                        inst_to_emit |= 0x40000000;
                    }
                    inst_to_emit = id.code == INST_CODE.INST_PRFM_LOADLIT__LITERAL ? (int)((long)inst_to_emit | prfop.intValue() & 0x1FL) : (inst_to_emit |= regD.code & 0x1F);
                    inst_to_emit = (int)((long)inst_to_emit | (lbl.value() >> 2 & 0x7FFFL) << 5);
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case log_imm: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg() && o3.isImm()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Immediate imm = (Immediate)o3;
                    inst_to_emit = o1.size() == 64 ? (inst_to_emit |= Integer.MIN_VALUE) : (inst_to_emit &= 0xFFBFFFFF);
                    inst_to_emit |= regD.code & 0x1F;
                    inst_to_emit |= (regN.code & 0x1F) << 5;
                    inst_to_emit = o1.size() == 64 ? (int)((long)inst_to_emit | (imm.value() & 0x1FFFL) << 10) : (int)((long)inst_to_emit | (imm.value() & 0xFFFL) << 10);
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case log_shift: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isReg()) {
                    Register regD = (Register)o1;
                    Register regN = (Register)o2;
                    Register regM = null;
                    if (o3 != _none) {
                        regM = (Register)o3;
                    }
                    Shift sft = null;
                    if (o4 != _none) {
                        sft = (Shift)o4;
                    }
                    if (o1.size() == 64) {
                        inst_to_emit |= Integer.MIN_VALUE;
                    }
                    inst_to_emit |= regD.code & 0x1F;
                    if (id.code == INST_CODE.INST_MOV_LOG_SHIFT) {
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                    } else {
                        inst_to_emit |= (regN.code & 0x1F) << 5;
                        inst_to_emit |= (regM.code & 0x1F) << 16;
                    }
                    if (sft != null) {
                        inst_to_emit = (int)((long)inst_to_emit | (sft.value() & 0x3FL) << 10);
                        inst_to_emit = (int)((long)inst_to_emit | (sft.type() & 3L) << 22);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case movewide: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isImm()) {
                    Register regD = (Register)o1;
                    Immediate imm16 = (Immediate)o2;
                    Shift sft = null;
                    if (o3 != _none) {
                        sft = (Shift)o3;
                    }
                    if (o1.size() == 64) {
                        inst_to_emit |= Integer.MIN_VALUE;
                    }
                    inst_to_emit |= regD.code & 0x1F;
                    inst_to_emit = (int)((long)inst_to_emit | (imm16.value() & 0xFFFFL) << 5);
                    if (sft != null && sft.value() % 16L == 0L && sft.value() < 49L) {
                        inst_to_emit = (int)((long)inst_to_emit | (sft.value() >> 4 & 3L) << 21);
                    }
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case pcreladdr: {
                int inst_to_emit = 0;
                if (o1.isReg() && o2.isImm()) {
                    Register regD = (Register)o1;
                    Immediate imm16 = (Immediate)o2;
                    inst_to_emit |= regD.code & 0x1F;
                    long imm = imm16.value() >> 12;
                    inst_to_emit = (int)((long)inst_to_emit | (imm >> 2 & 0x7FFFFL) << 5);
                    inst_to_emit = (int)((long)inst_to_emit | (imm & 3L) << 29);
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
            case ic_system: {
                int inst_to_emit = 0;
                if (id.code == INST_CODE.INST_NOP_IC_SYSTEM || id.code == INST_CODE.INST_YIELD_IC_SYSTEM || id.code == INST_CODE.INST_WFE_IC_SYSTEM || id.code == INST_CODE.INST_WFI_IC_SYSTEM || id.code == INST_CODE.INST_SEV_IC_SYSTEM || id.code == INST_CODE.INST_SEVL_IC_SYSTEM) {
                    inst_to_emit |= id.opcode;
                } else if (id.code == INST_CODE.INST_HINT_IC_SYSTEM || id.code == INST_CODE.INST_CLREX_IC_SYSTEM || id.code == INST_CODE.INST_DSB_IC_SYSTEM || id.code == INST_CODE.INST_DMB_IC_SYSTEM || id.code == INST_CODE.INST_ISB_IC_SYSTEM) {
                    Immediate imm = (Immediate)o1;
                    inst_to_emit = id.code == INST_CODE.INST_HINT_IC_SYSTEM ? (int)((long)inst_to_emit | (imm.value() & 0x7FL) << 5) : (int)((long)inst_to_emit | (imm.value() & 0xFL) << 8);
                } else if (id.code == INST_CODE.INST_MSR_IC_SYSTEM_X) {
                    SysRegister sysrt = (SysRegister)o1;
                    Register rt = (Register)o2;
                    SysRegDescription sysregid = SysRegDescription.find(sysrt.getEnum());
                    inst_to_emit |= (sysregid.reg_code & 0xFFFF) << 5;
                    inst_to_emit |= rt.code & 0x1F;
                } else if (id.code == INST_CODE.INST_SYS_IC_SYSTEM || id.code == INST_CODE.INST_SYSL_IC_SYSTEM) {
                    Immediate imm3_op1 = (Immediate)o1;
                    Register Rt = (Register)o1;
                    Register cRn = (Register)o2;
                    Immediate imm3_op1_2 = (Immediate)o2;
                    Register cRm = (Register)o3;
                    Register cRn_2 = (Register)o3;
                    Immediate imm3_op2_4 = (Immediate)o4;
                    Register cRm_2 = (Register)o4;
                    Register rt = null;
                    Immediate imm3_op2_5_2 = null;
                    if (o5 != null) {
                        rt = (Register)o5;
                        imm3_op2_5_2 = (Immediate)o5;
                    }
                    inst_to_emit = (int)((long)inst_to_emit | ((id.code == INST_CODE.INST_SYS_IC_SYSTEM ? imm3_op1.value() : imm3_op1_2.value()) & 7L) << 16);
                    inst_to_emit |= ((id.code == INST_CODE.INST_SYS_IC_SYSTEM ? cRn.code() : cRn_2.code()) & 0xF) << 12;
                    inst_to_emit = (int)((long)(inst_to_emit |= ((id.code == INST_CODE.INST_SYS_IC_SYSTEM ? cRm.code() : cRm_2.code()) & 0xF) << 8) | ((id.code == INST_CODE.INST_SYS_IC_SYSTEM ? imm3_op2_4.value() : imm3_op2_5_2.value()) & 7L) << 5);
                    inst_to_emit = id.code == INST_CODE.INST_SYS_IC_SYSTEM ? (rt != null ? (inst_to_emit |= rt.code() & 0x1F) : (inst_to_emit |= 0x1F)) : (inst_to_emit |= Rt.code() & 0x1F);
                } else {
                    throw new IllegalArgumentException("illegal arguments");
                }
                this._emitInt32(inst_to_emit |= id.opcode);
                break;
            }
            case testbranch: {
                int inst_to_emit = 0;
                if (o1.isReg()) {
                    Register regD = (Register)o1;
                    Immediate imm = (Immediate)o2;
                    Immediate lbl_imm14 = (Immediate)o3;
                    inst_to_emit |= regD.code & 0x1F;
                    inst_to_emit = (int)((long)inst_to_emit | (imm.value() & 0x1FL) << 19);
                    inst_to_emit = (int)((long)inst_to_emit | (imm.value() >> 5 & 1L) << 31);
                    inst_to_emit = (int)((long)inst_to_emit | (lbl_imm14.value() & 0x3FFFL) << 5);
                    this._emitInt32(inst_to_emit |= id.opcode);
                    break;
                }
                throw new IllegalArgumentException("illegal arguments");
            }
        }
    }

    void _emitJmpOrCallReloc(InstructionGroup instruction, long target) {
        RelocData rd = new RelocData(RelocData.Type.ABSOLUTE_TO_RELATIVE_TRAMPOLINE, 4, this.offset(), target);
        this._relocData.add(rd);
        this._emitInt32(0);
    }

    public void relocCode(ByteBuffer buffer, long address) {
        int csize = this.codeSize();
        this._buffer.copyTo(buffer);
        block9: for (RelocData r : this._relocData) {
            long val;
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
                    if (r.destination - (address + (long)r.offset) > 0x8000000L) {
                        System.out.println("IMPOSSIBLE JUMP : ADDRESS AHEAD OF RANGE of 128MB");
                    }
                    if (r.destination - (address + (long)r.offset) < -134217728L) {
                        System.out.println("IMPOSSIBLE JUMP : ADDRESS BELOW OF RANGE of 128MB");
                    }
                    val = (r.destination - (address + (long)r.offset)) / 4L;
                    break;
                }
                default: {
                    throw new IllegalStateException("invalid relocation type");
                }
            }
            switch (r.size) {
                case 4: {
                    val &= 0x3FFFFFFL;
                    buffer.putInt(r.offset, (int)(val |= 0xFFFFFFFF94000000L));
                    continue block9;
                }
                case 8: {
                    buffer.putLong(r.offset, val);
                    continue block9;
                }
            }
            throw new IllegalStateException("invalid relocation size");
        }
    }
}

