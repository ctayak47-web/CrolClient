
package jnr.a64asm;

import jnr.a64asm.Conditions;
import jnr.a64asm.Ext;
import jnr.a64asm.INST_CODE;
import jnr.a64asm.Immediate;
import jnr.a64asm.Label;
import jnr.a64asm.Mem;
import jnr.a64asm.Offset;
import jnr.a64asm.PRFOP_ENUM;
import jnr.a64asm.Post_index;
import jnr.a64asm.Pre_index;
import jnr.a64asm.Register;
import jnr.a64asm.SerializerCore;
import jnr.a64asm.Shift;
import jnr.a64asm.SysRegister;

public abstract class SerializerIntrinsics_a64
extends SerializerCore {
    public final void adc(Register dst, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_ADC_ADDSUB_CARRY, dst, Xn, Xm);
    }

    public final void adcs(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_ADCS_ADDSUB_CARRY, Xd, Xn, Xm);
    }

    public final void add(Register Xd, Register Xn, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_ADD_ADDSUB_IMM, Xd, Xn, val, sft);
    }

    public final void add(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_ADD_ADDSUB_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void add(Register Xd, Register Xn, Register Xm, Ext extnd) {
        this.emitA64(INST_CODE.INST_ADD_EXT_ADDSUB_EXT, Xd, Xn, Xm, extnd);
    }

    public final void adds(Register Xd, Register Xn, Register Xm, Ext extnd) {
        this.emitA64(INST_CODE.INST_ADDS_ADDSUB_EXT, Xd, Xn, Xm, extnd);
    }

    public final void adds(Register Xd, Register Xn, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_ADDS_ADDSUB_IMM, Xd, Xn, val, sft);
    }

    public final void adds(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_ADDS_ADDSUB_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void adr(Register Xd, Label label) {
        this.emitA64(INST_CODE.INST_ADR_PCRELADDR, Xd, label);
    }

    public final void adrp(Register Xd, Label label) {
        this.emitA64(INST_CODE.INST_ADRP_PCRELADDR, Xd, label);
    }

    public final void and(Register Xd, Register Xn, Immediate val) {
        this.emitA64(INST_CODE.INST_AND_LOG_IMM, Xd, Xn, val);
    }

    public final void and(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_AND_LOG_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void ands(Register Xd, Register Xn, Immediate val) {
        this.emitA64(INST_CODE.INST_ANDS_LOG_IMM, Xd, Xn, val);
    }

    public final void ands(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_ANDS_LOG_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void asr(Register Xd, Register Xn, Immediate val) {
        this.emitA64(INST_CODE.INST_ASR_BITFIELD, Xd, Xn, val);
    }

    public final void asr(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_ASR_DP_2SRC, Xd, Xn, Xm);
    }

    public final void asrv(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_ASRV_DP_2SRC, Xd, Xn, Xm);
    }

    public final void b(Immediate Xd) {
        this.emitA64(INST_CODE.INST_B_BRANCH_IMM, Xd);
    }

    public final void bcc(Immediate Xd) {
        this.emitA64(INST_CODE.INST_BCC_CONDBRANCH, Xd);
    }

    public final void bcs(Immediate Xd) {
        this.emitA64(INST_CODE.INST_BCS_CONDBRANCH, Xd);
    }

    public final void beq(Immediate imm19) {
        this.emitA64(INST_CODE.INST_BEQ_CONDBRANCH, imm19);
    }

    public final void bfi(Register Xd, Register Xn, Immediate val, Immediate width) {
        this.emitA64(INST_CODE.INST_BFI_BITFIELD, Xd, Xn, val, width);
    }

    public final void bfm(Register Xd, Register Xn, Immediate val1, Immediate val2) {
        this.emitA64(INST_CODE.INST_BFM_BITFIELD, Xd, Xn, val1, val2);
    }

    public final void bfxil(Register Xd, Register Xn, Immediate val, Immediate width) {
        this.emitA64(INST_CODE.INST_BFXIL_BITFIELD, Xd, Xn, val, width);
    }

    public final void bge(Immediate label) {
        this.emitA64(INST_CODE.INST_BGE_CONDBRANCH, label);
    }

    public final void bgt(Immediate label) {
        this.emitA64(INST_CODE.INST_BGT_CONDBRANCH, label);
    }

    public final void bhi(Immediate label) {
        this.emitA64(INST_CODE.INST_BHI_CONDBRANCH, label);
    }

    public final void bhs(Immediate label) {
        this.emitA64(INST_CODE.INST_BHS_CONDBRANCH, label);
    }

    public final void bic(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_BIC_LOG_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void bics(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_BICS_LOG_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void bl(Immediate label) {
        this.emitA64(INST_CODE.INST_BL_BRANCH_IMM, label);
    }

    public final void ble(Immediate label) {
        this.emitA64(INST_CODE.INST_BLE_CONDBRANCH, label);
    }

    public final void blo(Immediate label) {
        this.emitA64(INST_CODE.INST_BLO_CONDBRANCH, label);
    }

    public final void blr(Register Xn) {
        this.emitA64(INST_CODE.INST_BLR_BRANCH_REG, Xn);
    }

    public final void bls(Immediate label) {
        this.emitA64(INST_CODE.INST_BLS_CONDBRANCH, label);
    }

    public final void blt(Immediate label) {
        this.emitA64(INST_CODE.INST_BLT_CONDBRANCH, label);
    }

    public final void bmi(Immediate label) {
        this.emitA64(INST_CODE.INST_BMI_CONDBRANCH, label);
    }

    public final void bne(Immediate label) {
        this.emitA64(INST_CODE.INST_BNE_CONDBRANCH, label);
    }

    public final void bpl(Immediate label) {
        this.emitA64(INST_CODE.INST_BPL_CONDBRANCH, label);
    }

    public final void br(Register Xn) {
        this.emitA64(INST_CODE.INST_BR_BRANCH_REG, Xn);
    }

    public final void brk(Immediate val) {
        this.emitA64(INST_CODE.INST_BRK_EXCEPTION, val);
    }

    public final void bvc(Immediate label) {
        this.emitA64(INST_CODE.INST_BVC_CONDBRANCH, label);
    }

    public final void bvs(Immediate label) {
        this.emitA64(INST_CODE.INST_BVS_CONDBRANCH, label);
    }

    public final void cbnz(Register Xn, Label label) {
        this.emitA64(INST_CODE.INST_CBNZ_COMPBRANCH, Xn, label);
    }

    public final void cbz(Register Xn, Label label) {
        this.emitA64(INST_CODE.INST_CBZ_COMPBRANCH, Xn, label);
    }

    public final void ccmn(Register Xn, Immediate val, Immediate nzcv, Conditions cc) {
        this.emitA64(INST_CODE.INST_CCMN_CONDCMP_IMM, Xn, val, nzcv, cc);
    }

    public final void ccmn(Register Xn, Register Xm, Immediate nzcv, Conditions cc) {
        this.emitA64(INST_CODE.INST_CCMN_CONDCMP_REG, Xn, Xm, nzcv, cc);
    }

    public final void ccmp(Register Xn, Immediate val, Immediate nzcv, Conditions cc) {
        this.emitA64(INST_CODE.INST_CCMP_CONDCMP_IMM, Xn, val, nzcv, cc);
    }

    public final void ccmp(Register Xn, Register Xm, Immediate nzcv, Conditions cc) {
        this.emitA64(INST_CODE.INST_CCMP_CONDCMP_REG, Xn, Xm, nzcv, cc);
    }

    public final void cinc(Register Xd, Register Xn, Conditions cc) {
        this.emitA64(INST_CODE.INST_CINC_CONDSEL, Xd, Xn, cc);
    }

    public final void cinv(Register Xd, Register Xn, Conditions cc) {
        this.emitA64(INST_CODE.INST_CINV_CONDSEL, Xd, Xn, cc);
    }

    public final void clrex(Immediate val) {
        this.emitA64(INST_CODE.INST_CLREX_IC_SYSTEM, val);
    }

    public final void cls(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_CLS_DP_1SRC, Xd, Xn);
    }

    public final void clz(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_CLZ_DP_1SRC, Xd, Xn);
    }

    public final void cmn(Register Xd, Register Xn, Ext extend) {
        this.emitA64(INST_CODE.INST_CMN_ADDSUB_EXT, Xd, Xn, extend);
    }

    public final void cmn(Register Xd, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_CMN_ADDSUB_IMM, Xd, val, sft);
    }

    public final void cmn(Register Xd, Register Xn, Shift sft) {
        this.emitA64(INST_CODE.INST_CMN_ADDSUB_SHIFT, Xd, Xn, sft);
    }

    public final void cmp(Register Xd, Register Xn, Ext extend) {
        this.emitA64(INST_CODE.INST_CMP_ADDSUB_EXT, Xd, Xn, extend);
    }

    public final void cmp(Register Xd, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_CMP_ADDSUB_IMM, Xd, val, sft);
    }

    public final void cmp(Register Xd, Register Xn, Shift sft) {
        this.emitA64(INST_CODE.INST_CMP_ADDSUB_SHIFT, Xd, Xn, sft);
    }

    public final void cneg(Register Xd, Register Xn, Conditions cc) {
        this.emitA64(INST_CODE.INST_CNEG_CONDSEL, Xd, Xn, cc);
    }

    public final void csel(Register Xd, Register Xn, Register Xm, Conditions cc) {
        this.emitA64(INST_CODE.INST_CSEL_CONDSEL, Xd, Xn, Xm, cc);
    }

    public final void cset(Register Xd, Conditions cc) {
        this.emitA64(INST_CODE.INST_CSET_CONDSEL, Xd, cc);
    }

    public final void csetm(Register Xd, Conditions cc) {
        this.emitA64(INST_CODE.INST_CSETM_CONDSEL, Xd, cc);
    }

    public final void csinc(Register Xd, Register Xn, Register Xm, Conditions cc) {
        this.emitA64(INST_CODE.INST_CSINC_CONDSEL, Xd, Xn, Xm, cc);
    }

    public final void csinv(Register Xd, Register Xn, Register Xm, Conditions cc) {
        this.emitA64(INST_CODE.INST_CSINV_CONDSEL, Xd, Xn, Xm, cc);
    }

    public final void csneg(Register Xd, Register Xn, Register Xm, Conditions cc) {
        this.emitA64(INST_CODE.INST_CSNEG_CONDSEL, Xd, Xn, Xm, cc);
    }

    public final void dc(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_DC_IC_SYSTEM, Xd, Xn);
    }

    public final void dcps1(Immediate val) {
        this.emitA64(INST_CODE.INST_DCPS1_EXCEPTION, val);
    }

    public final void dcps2(Immediate val) {
        this.emitA64(INST_CODE.INST_DCPS2_EXCEPTION, val);
    }

    public final void dcps3(Immediate val) {
        this.emitA64(INST_CODE.INST_DCPS3_EXCEPTION, val);
    }

    public final void dmb(Immediate val) {
        this.emitA64(INST_CODE.INST_DMB_IC_SYSTEM, val);
    }

    public final void drps() {
        this.emitA64(INST_CODE.INST_DRPS_BRANCH_REG);
    }

    public final void dsb(Immediate val) {
        this.emitA64(INST_CODE.INST_DSB_IC_SYSTEM, val);
    }

    public final void eon(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_EON_LOG_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void eor(Register Xd, Register Xn, Immediate val) {
        this.emitA64(INST_CODE.INST_EOR_LOG_IMM, Xd, Xn, val);
    }

    public final void eor(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_EOR_LOG_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void eret() {
        this.emitA64(INST_CODE.INST_ERET_BRANCH_REG);
    }

    public final void extr(Register Xd, Register Xn, Register Xm, Immediate val) {
        this.emitA64(INST_CODE.INST_EXTR_EXTRACT, Xd, Xn, Xm, val);
    }

    public final void hint(Immediate val) {
        this.emitA64(INST_CODE.INST_HINT_IC_SYSTEM, val);
    }

    public final void hlt(Immediate val) {
        this.emitA64(INST_CODE.INST_HLT_EXCEPTION, val);
    }

    public final void hvc(Immediate val) {
        this.emitA64(INST_CODE.INST_HVC_EXCEPTION, val);
    }

    public final void ic(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_IC_IC_SYSTEM, Xd, Xn);
    }

    public final void isb(Immediate val) {
        this.emitA64(INST_CODE.INST_ISB_IC_SYSTEM, val);
    }

    public final void ldar(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDAR_LDSTEXCL, Xd, location);
    }

    public final void ldarb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDARB_LDSTEXCL, Xd, location);
    }

    public final void ldarh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDARH_LDSTEXCL, Xd, location);
    }

    public final void ldaxp(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDAXP_LDSTEXCL, Xd, location);
    }

    public final void ldaxr(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDAXR_LDSTEXCL, Xd, location);
    }

    public final void ldaxrb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDAXRB_LDSTEXCL, Xd, location);
    }

    public final void ldaxrh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDAXRH_LDSTEXCL, Xd, location);
    }

    public final void ldnp(Register Xd, Register Xn, Register location, Immediate imm7) {
        this.emitA64(INST_CODE.INST_LDNP_LDSTNAPAIR_OFFS, Xd, Xn, location, imm7);
    }

    public final void ldp(Register Xd, Register Xn, Post_index location) {
        this.emitA64(INST_CODE.INST_LDP_POST_INDEXED_IDST_IMM9, Xd, Xn, location);
    }

    public final void ldp(Register Xd, Register Xn, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_LDP_PRE_INDEXED_IDST_IMM9, Xd, Xn, pindex);
    }

    public final void ldp(Register Xd, Register Xn, Offset offset) {
        this.emitA64(INST_CODE.INST_LDP_LDSTPAIR_OFF_LDST_POS, Xd, Xn, offset);
    }

    public final void ldpsw(Register Xd, Register Xn, Mem location, Immediate val) {
        this.emitA64(INST_CODE.INST_LDPSW_POST_INDEXED, Xd, Xn, location, val);
    }

    public final void ldpsw(Register Xd, Register Xn, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_LDPSW_PRE_INDEXED, Xd, Xn, pindex);
    }

    public final void ldpsw(Register Xd, Register Xn, Offset offset) {
        this.emitA64(INST_CODE.INST_LDPSW_OFF, Xd, Xn, offset);
    }

    public final void ldr(Register Xd, Post_index postindex) {
        this.emitA64(INST_CODE.INST_LDR_IMM_POST, Xd, postindex);
    }

    public final void ldr(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_LDR_IMM_PRE, Xd, pindex);
    }

    public final void ldr(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_LDR_IMM_OFF, Xd, offset);
    }

    public final void ldr(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDR_REG, Xd, location);
    }

    public final void ldr(Register Xd, Immediate label) {
        this.emitA64(INST_CODE.INST_LDR_LOADLIT, label);
    }

    public final void ldrb(Register Xd, Mem location, Immediate val) {
        this.emitA64(INST_CODE.INST_LDRB_IMM_POST, Xd, location, val);
    }

    public final void ldrb(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_LDRB_IMM_PRE, Xd, pindex);
    }

    public final void ldrb(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_LDRB_IMM_OFF, Xd, offset);
    }

    public final void ldrb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDRB_REG, Xd, location);
    }

    public final void ldrh(Register Xd, Mem location, Immediate val) {
        this.emitA64(INST_CODE.INST_LDRH_IMM_POST, Xd, location, val);
    }

    public final void ldrh(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_LDRH_IMM_PRE, Xd, pindex);
    }

    public final void ldrh(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_LDRH_IMM_OFF, Xd, offset);
    }

    public final void ldrh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDRH_REG, Xd, location);
    }

    public final void ldrsb(Register Xd, Mem location, Immediate val) {
        this.emitA64(INST_CODE.INST_LDRSB_IMM_POST, Xd, location, val);
    }

    public final void ldrsb(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_LDRSB_IMM_PRE, Xd, pindex);
    }

    public final void ldrsb(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_LDRSB_IMM_OFF, Xd, offset);
    }

    public final void ldrsb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDRSB_REG, Xd, location);
    }

    public final void ldrsh(Register Xd, Mem location, Immediate val) {
        this.emitA64(INST_CODE.INST_LDRSH_IMM_POST, Xd, location, val);
    }

    public final void ldrsh(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_LDRSH_IMM_PRE, Xd, pindex);
    }

    public final void ldrsh(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_LDRSH_IMM_OFF, Xd, offset);
    }

    public final void ldrsh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDRSH_REG, Xd, location);
    }

    public final void ldrsw(Register Xd, Mem location, Immediate val) {
        this.emitA64(INST_CODE.INST_LDRSW_IMM_POST, Xd, location, val);
    }

    public final void ldrsw(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_LDRSW_IMM_PRE, Xd, pindex);
    }

    public final void ldrsw(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_LDRSW_IMM_OFF, Xd, offset);
    }

    public final void ldrsw(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDRSW_REG, Xd, location);
    }

    public final void ldrsw(Register Xd, Label label) {
        this.emitA64(INST_CODE.INST_LDRSW_LOADLIT, label);
    }

    public final void ldtr(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDTR_LDST_UNPRIV, Xd, location);
    }

    public final void ldtrb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDTRB_LDST_UNPRIV, Xd, location);
    }

    public final void ldtrh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDTRH_LDST_UNPRIV, Xd, location);
    }

    public final void ldtrsb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDTRSB_LDST_UNPRIV, Xd, location);
    }

    public final void ldtrsh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDTRSH_LDST_UNPRIV, Xd, location);
    }

    public final void ldtrsw(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDTRSW_LDST_UNPRIV, Xd, location);
    }

    public final void ldur(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDUR_LDST_UNSCALED_X, Xd, location);
    }

    public final void ldurb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDURB_LDST_UNSCALED, Xd, location);
    }

    public final void ldurh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDURH_LDST_UNSCALED, Xd, location);
    }

    public final void ldursb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDURSB_LDST_UNSCALED, Xd, location);
    }

    public final void ldursh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDURSH_LDST_UNSCALED, Xd, location);
    }

    public final void ldursw(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDURSW_LDST_UNSCALED, Xd, location);
    }

    public final void ldxp(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDXP_LDSTEXCL, Xd, location);
    }

    public final void ldxr(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDXR_LDSTEXCL, Xd, location);
    }

    public final void ldxrb(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDXRB_LDSTEXCL, Xd, location);
    }

    public final void ldxrh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_LDXRH_LDSTEXCL, Xd, location);
    }

    public final void lsl(Register Xd, Register Xn, Immediate val) {
        this.emitA64(INST_CODE.INST_LSL_BITFIELD, Xd, Xn, val);
    }

    public final void lsl(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_LSL_DP_2SRC, Xd, Xn, Xm);
    }

    public final void lslv(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_LSLV_DP_2SRC, Xd, Xn, Xm);
    }

    public final void lsr(Register Xd, Register Xn, Immediate val) {
        this.emitA64(INST_CODE.INST_LSR_BITFIELD, Xd, Xn, val);
    }

    public final void lsr(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_LSR_DP_2SRC, Xd, Xn, Xm);
    }

    public final void lsrv(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_LSRV_DP_2SRC, Xd, Xn, Xm);
    }

    public final void madd(Register Xd, Register Xn, Register Xm, Register Xa) {
        this.emitA64(INST_CODE.INST_MADD_DP_3SRC, Xd, Xn, Xm, Xa);
    }

    public final void mneg(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_MNEG_DP_3SRC, Xd, Xn, Xm);
    }

    public final void mov(Register Xd, Register Xn) {
        if (Xd.code() < 31 && Xn.code() < 31) {
            this.emitA64(INST_CODE.INST_MOV_ADDSUB_IMM, Xd, Xn);
        } else {
            this.emitA64(INST_CODE.INST_MOV_LOG_SHIFT, Xd, Xn);
        }
    }

    public final void mov(Register Xd, Shift sft) {
        this.emitA64(INST_CODE.INST_MOV_LOG_SHIFT, Xd, sft);
    }

    public final void mov(Register Xd, Immediate val) {
        if (Xd.code() < 31) {
            this.emitA64(INST_CODE.INST_MOV_MOVEWIDE_X, Xd, val);
        } else {
            this.emitA64(INST_CODE.INST_MOV_LOG_IMM, Xd, val);
        }
    }

    public final void movk(Register Xd, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_MOVK_MOVEWIDE, Xd, val, sft);
    }

    public final void movn(Register Xd, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_MOVN_MOVEWIDE, Xd, val, sft);
    }

    public final void movz(Register Xd, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_MOVZ_MOVEWIDE, Xd, val, sft);
    }

    public final void mrs(Register Xd, Register SysReg) {
        this.emitA64(INST_CODE.INST_MRS_IC_SYSTEM, Xd, SysReg);
    }

    public final void msr(Register Xd, Immediate val) {
        this.emitA64(INST_CODE.INST_MSR_IC_SYSTEM, Xd, val);
    }

    public final void msr(SysRegister srt, Register Xd) {
        this.emitA64(INST_CODE.INST_MSR_IC_SYSTEM_X, srt, Xd);
    }

    public final void msub(Register Xd, Register Xn, Register Xm, Register Xa) {
        this.emitA64(INST_CODE.INST_MSUB_DP_3SRC, Xd, Xn, Xm, Xa);
    }

    public final void mul(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_MUL_DP_3SRC, Xd, Xn, Xm);
    }

    public final void mvn(Register Xd, Register Xn, Shift sft) {
        this.emitA64(INST_CODE.INST_MVN_LOG_SHIFT, Xd, Xn, sft);
    }

    public final void neg(Register Xd, Register Xn, Shift sft) {
        this.emitA64(INST_CODE.INST_NEG_ADDSUB_SHIFT, Xd, Xn, sft);
    }

    public final void negs(Register Xd, Register Xn, Shift sft) {
        this.emitA64(INST_CODE.INST_NEGS_ADDSUB_SHIFT, Xd, Xn, sft);
    }

    public final void ngc(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_NGC_ADDSUB_CARRY, Xd, Xn);
    }

    public final void ngcs(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_NGCS_ADDSUB_CARRY, Xd, Xn);
    }

    public final void nop() {
        this.emitA64(INST_CODE.INST_NOP_IC_SYSTEM);
    }

    public final void orn(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_ORN_LOG_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void orr(Register Xd, Register Xm, Immediate val) {
        this.emitA64(INST_CODE.INST_ORR_LOG_IMM, Xd, Xm, val);
    }

    public final void orr(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_ORR_LOG_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void prfm(PRFOP_ENUM Xd, Register Xn, Immediate imm12) {
        this.emitA64(INST_CODE.INST_PRFM_LDST_POS__IMMEDIATE, Xd, Xn, imm12);
    }

    public final void prfm(PRFOP_ENUM Xd, Immediate imm19) {
        this.emitA64(INST_CODE.INST_PRFM_LOADLIT__LITERAL, Xd, imm19);
    }

    public final void prfm(PRFOP_ENUM label, Register Xn, Register Rm, Ext ext) {
        this.emitA64(INST_CODE.INST_PRFM_LDST_REGOFF__REGISTER, label, Xn, Rm, ext);
    }

    public final void prfum(PRFOP_ENUM Xd, Register Xn, Immediate imm9) {
        this.emitA64(INST_CODE.INST_PRFUM_LDST_UNSCALED, Xd, Xn, imm9);
    }

    public final void rbit(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_RBIT_DP_1SRC, Xd, Xn);
    }

    public final void ret(Register Xd) {
        this.emitA64(INST_CODE.INST_RET_BRANCH_REG, Xd);
    }

    public final void rev(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_REV_DP_1SRC_X, Xd, Xn);
    }

    public final void rev16(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_REV16_DP_1SRC, Xd, Xn);
    }

    public final void rev32(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_REV32_DP_1SRC, Xd, Xn);
    }

    public final void ror(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_ROR_DP_2SRC, Xd, Xn, Xm);
    }

    public final void ror(Register Xd, Register Xm, Immediate val) {
        this.emitA64(INST_CODE.INST_ROR_EXTRACT, Xd, Xm, val);
    }

    public final void rorv(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_RORV_DP_2SRC, Xd, Xn, Xm);
    }

    public final void sbc(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_SBC_ADDSUB_CARRY, Xd, Xn, Xm);
    }

    public final void sbcs(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_SBCS_ADDSUB_CARRY, Xd, Xn, Xm);
    }

    public final void sbfiz(Register Xd, Register Xn, Immediate lsb, Immediate width) {
        this.emitA64(INST_CODE.INST_SBFIZ_BITFIELD, Xd, Xn, lsb, width);
    }

    public final void sbfm(Register Xd, Register Xn, Immediate val, Immediate val1) {
        this.emitA64(INST_CODE.INST_SBFM_BITFIELD, Xd, Xn, val, val1);
    }

    public final void sbfx(Register Xd, Register Xn, Immediate val, Immediate val1) {
        this.emitA64(INST_CODE.INST_SBFX_BITFIELD, Xd, Xn, val, val1);
    }

    public final void sdiv(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_SDIV_DP_2SRC, Xd, Xn, Xm);
    }

    public final void sev() {
        this.emitA64(INST_CODE.INST_SEV_IC_SYSTEM);
    }

    public final void sevl() {
        this.emitA64(INST_CODE.INST_SEVL_IC_SYSTEM);
    }

    public final void smaddl(Register Xd, Register Wn, Register Wm, Register Xn) {
        this.emitA64(INST_CODE.INST_SMADDL_DP_3SRC, Xd, Wn, Wm, Xn);
    }

    public final void smc(Immediate val) {
        this.emitA64(INST_CODE.INST_SMC_EXCEPTION, val);
    }

    public final void smnegl(Register Xd, Register Wn, Register Wm) {
        this.emitA64(INST_CODE.INST_SMNEGL_DP_3SRC, Xd, Wn, Wm);
    }

    public final void smsubl(Register Xd, Register Wn, Register Wm, Register Xn) {
        this.emitA64(INST_CODE.INST_SMSUBL_DP_3SRC, Xd, Wn, Wm, Xn);
    }

    public final void smulh(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_SMULH_DP_3SRC, Xd, Xn, Xm);
    }

    public final void smull(Register Xd, Register Wn, Register Wm) {
        this.emitA64(INST_CODE.INST_SMULL_DP_3SRC, Xd, Wn, Wm);
    }

    public final void stlr(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_STLR_LDSTEXCL, Xd, location);
    }

    public final void stlrb(Register Xd, Register Xn, Immediate val) {
        this.emitA64(INST_CODE.INST_STLRB_LDSTEXCL, Xd, Xn, val);
    }

    public final void stlrh(Register Xd, Mem location) {
        this.emitA64(INST_CODE.INST_STLRH_LDSTEXCL, Xd, location);
    }

    public final void stlxp(Register Wd, Register Xn, Register Xm, Mem location) {
        this.emitA64(INST_CODE.INST_STLXP_LDSTEXCL, Wd, Xn, Xm, location);
    }

    public final void stlxr(Register Wd, Register Xn, Mem location) {
        this.emitA64(INST_CODE.INST_STLXR_LDSTEXCL, Wd, Xn, location);
    }

    public final void stlxrb(Register Wd, Register Wn, Mem location) {
        this.emitA64(INST_CODE.INST_STLXRB_LDSTEXCL, Wd, Wn, location);
    }

    public final void stlxrh(Register Wd, Register Wn, Mem location) {
        this.emitA64(INST_CODE.INST_STLXRH_LDSTEXCL, Wd, Wn, location);
    }

    public final void stnp(Register Xd, Register Xn, Mem location) {
        this.emitA64(INST_CODE.INST_STNP_LDSTNAPAIR_OFFS, Xd, Xn, location);
    }

    public final void stp(Register Xd, Register Xn, Post_index location) {
        this.emitA64(INST_CODE.INST_STP_LDSTPAIR_INDEXED_POST, Xd, Xn, location);
    }

    public final void stp(Register Xd, Register Xn, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_STP_LDSTPAIR_INDEXED_PRE, Xd, Xn, pindex);
    }

    public final void stp(Register Xd, Register Xn, Offset offset) {
        this.emitA64(INST_CODE.INST_STP_LDSTPAIR_OFF, Xd, Xn, offset);
    }

    public final void str(Register Xd, Post_index postindex) {
        this.emitA64(INST_CODE.INST_STR_LDST_IMM9_POST, Xd, postindex);
    }

    public final void str(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_STR_LDST_IMM9_PRE, Xd, pindex);
    }

    public final void str(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STR_LDST_POS, Xd, offset);
    }

    public final void str(Register Xt, Register Xn, Register Xm, Ext ext) {
        this.emitA64(INST_CODE.INST_STR_LDST_REGOFF, Xt, Xn, Xm, ext);
    }

    public final void strb(Register Xd, Post_index pindex) {
        this.emitA64(INST_CODE.INST_STRB_LDST_IMM9_POST, Xd, pindex);
    }

    public final void strb(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_STRB_LDST_PRE, Xd, pindex);
    }

    public final void strb(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STRB_LDST_OFFSET, Xd, offset);
    }

    public final void strb(Register Wt, Register Xn, Register Rm, Ext ext) {
        this.emitA64(INST_CODE.INST_STRB_LDST_REGOFF, Wt, Xn, Rm, ext);
    }

    public final void strh(Register Xd, Post_index pindex) {
        this.emitA64(INST_CODE.INST_STRH_LDST_IMM_POST, Xd, pindex);
    }

    public final void strh(Register Xd, Pre_index pindex) {
        this.emitA64(INST_CODE.INST_STRH_LDST_IMM_PRE, Xd, pindex);
    }

    public final void strh(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STRH_LDST_IMM_OFF, Xd, offset);
    }

    public final void strh(Register Xd, Register Xn, Register Xm, Ext ext) {
        this.emitA64(INST_CODE.INST_STRH_LDST_REGOFF, Xd, Xn, Xm, ext);
    }

    public final void sttr(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STTR_LDST_UNPRIV, Xd, offset);
    }

    public final void sttrb(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STTRB_LDST_UNPRIV, Xd, offset);
    }

    public final void sttrh(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STTRH_LDST_UNPRIV, Xd, offset);
    }

    public final void stur(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STUR_LDST_UNSCALED_X, Xd, offset);
    }

    public final void sturb(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STURB_LDST_UNSCALED, Xd, offset);
    }

    public final void sturh(Register Xd, Offset offset) {
        this.emitA64(INST_CODE.INST_STURH_LDST_UNSCALED, Xd, offset);
    }

    public final void stxp(Register Xd, Register Xn, Register Xm, Register location, Immediate zero) {
        this.emitA64(INST_CODE.INST_STXP_LDSTEXCL, Xd, Xn, Xm, location, zero);
    }

    public final void stxr(Register Xd, Register Xn, Offset location) {
        this.emitA64(INST_CODE.INST_STXR_LDSTEXCL, Xd, Xn, location);
    }

    public final void stxrb(Register Xd, Register Xn, Offset location) {
        this.emitA64(INST_CODE.INST_STXRB_LDSTEXCL, Xd, Xn, location);
    }

    public final void stxrh(Register Xd, Register Xn, Offset location) {
        this.emitA64(INST_CODE.INST_STXRH_LDSTEXCL, Xd, Xn, location);
    }

    public final void sub(Register Xd, Register Xn, Register Xm, Ext extend) {
        this.emitA64(INST_CODE.INST_SUB_ADDSUB_EXT, Xd, Xn, Xm, extend);
    }

    public final void sub(Register Xd, Register Xn, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_SUB_ADDSUB_IMM, Xd, Xn, val, sft);
    }

    public final void sub(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_SUB_ADDSUB_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void subs(Register Xd, Register Xn, Register Xm, Ext extend) {
        this.emitA64(INST_CODE.INST_SUBS_ADDSUB_EXT, Xd, Xn, Xm, extend);
    }

    public final void subs(Register Xd, Register Xn, Immediate val, Shift sft) {
        this.emitA64(INST_CODE.INST_SUBS_ADDSUB_IMM, Xd, Xn, val, sft);
    }

    public final void subs(Register Xd, Register Xn, Register Xm, Shift sft) {
        this.emitA64(INST_CODE.INST_SUBS_ADDSUB_SHIFT, Xd, Xn, Xm, sft);
    }

    public final void svc(Immediate val) {
        this.emitA64(INST_CODE.INST_SVC_EXCEPTION, val);
    }

    public final void sxtb(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_SXTB_BITFIELD, Xd, Xn);
    }

    public final void sxth(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_SXTH_BITFIELD, Xd, Xn);
    }

    public final void sxtw(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_SXTW_BITFIELD, Xd, Xn);
    }

    public final void tbnz(Register Xd, Immediate val, Label label) {
        this.emitA64(INST_CODE.INST_TBNZ_TESTBRANCH, Xd, val, label);
    }

    public final void tbz(Register Xd, Immediate val, Label label) {
        this.emitA64(INST_CODE.INST_TBZ_TESTBRANCH, Xd, val, label);
    }

    public final void tst(Register Xd, Immediate val) {
        this.emitA64(INST_CODE.INST_TST_LOG_IMM, Xd, val);
    }

    public final void tst(Register Xd, Register Xn, Shift sft) {
        this.emitA64(INST_CODE.INST_TST_LOG_SHIFT, Xd, Xn, sft);
    }

    public final void ubfiz(Register Xd, Register Xn, Immediate val, Immediate val1) {
        this.emitA64(INST_CODE.INST_UBFIZ_BITFIELD, Xd, Xn, val, val1);
    }

    public final void ubfm(Register Xd, Register Xn, Immediate val, Immediate val1) {
        this.emitA64(INST_CODE.INST_UBFM_BITFIELD, Xd, Xn, val, val1);
    }

    public final void ubfx(Register Xd, Register Xn, Immediate val, Immediate val1) {
        this.emitA64(INST_CODE.INST_UBFX_BITFIELD, Xd, Xn, val, val1);
    }

    public final void udiv(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_UDIV_DP_2SRC, Xd, Xn, Xm);
    }

    public final void umaddl(Register Xd, Register Xn, Register Xm, Register Xa) {
        this.emitA64(INST_CODE.INST_UMADDL_DP_3SRC, Xd, Xn, Xm, Xa);
    }

    public final void umnegl(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_UMNEGL_DP_3SRC, Xd, Xn, Xm);
    }

    public final void umsubl(Register Xd, Register Xn, Register Xm, Register Xa) {
        this.emitA64(INST_CODE.INST_UMSUBL_DP_3SRC, Xd, Xn, Xm, Xa);
    }

    public final void umulh(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_UMULH_DP_3SRC, Xd, Xn, Xm);
    }

    public final void umull(Register Xd, Register Xn, Register Xm) {
        this.emitA64(INST_CODE.INST_UMULL_DP_3SRC, Xd, Xn, Xm);
    }

    public final void uxtb(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_UXTB_BITFIELD, Xd, Xn);
    }

    public final void uxth(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_UXTH_BITFIELD, Xd, Xn);
    }

    public final void uxtw(Register Xd, Register Xn) {
        this.emitA64(INST_CODE.INST_UXTW_LOG_SHIFT, Xd, Xn);
    }

    public final void wfe() {
        this.emitA64(INST_CODE.INST_WFE_IC_SYSTEM);
    }

    public final void wfi() {
        this.emitA64(INST_CODE.INST_WFI_IC_SYSTEM);
    }

    public final void yield() {
        this.emitA64(INST_CODE.INST_YIELD_IC_SYSTEM);
    }
}

