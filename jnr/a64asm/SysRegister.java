
package jnr.a64asm;

import jnr.a64asm.Operand;
import jnr.a64asm.SYSREG_CODE;

public class SysRegister
extends Operand {
    SYSREG_CODE sysRegEnum;
    private static final SysRegister[] sys = new SysRegister[305];

    public SysRegister(SYSREG_CODE sysRegEnum) {
        super(9, 64);
        this.sysRegEnum = sysRegEnum;
    }

    public static final SysRegister sysReg(SYSREG_CODE reg) {
        return sys[reg.ordinal()];
    }

    public SYSREG_CODE getEnum() {
        return this.sysRegEnum;
    }

    static {
        SYSREG_CODE i = SYSREG_CODE.SPSR_EL1;
        while (i.ordinal() < SYSREG_CODE.SYSREG_MAX.ordinal()) {
            SysRegister.sys[i.ordinal()] = new SysRegister(i);
            i = SYSREG_CODE.valueOf(i.ordinal() + 1);
        }
    }
}

