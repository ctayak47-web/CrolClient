
package com.kenai.jnr.x86asm;

import com.kenai.jnr.x86asm.BaseReg;

@Deprecated
public final class Register
extends BaseReg {
    private static final Register[] gpb = new Register[16];
    private static final Register[] gpw = new Register[16];
    private static final Register[] gpd = new Register[16];
    private static final Register[] gpq = new Register[16];

    Register(int code, int size) {
        super(code, size);
    }

    public static final Register gpr(int reg) {
        switch (reg & 0xF0) {
            case 0: {
                return gpb[reg & 0xF];
            }
            case 16: {
                return gpw[reg & 0xF];
            }
            case 32: {
                return gpd[reg & 0xF];
            }
            case 48: {
                return gpq[reg & 0xF];
            }
        }
        throw new IllegalArgumentException("invalid register 0x" + Integer.toHexString(reg));
    }

    private static final Register gpr(Register[] cache, int idx) {
        if (idx >= 0 && idx < 16) {
            return cache[idx];
        }
        throw new IllegalArgumentException("invalid register index " + idx);
    }

    public static final Register gpb(int idx) {
        return Register.gpr(gpb, idx);
    }

    public static final Register gpw(int idx) {
        return Register.gpr(gpw, idx);
    }

    public static final Register gpd(int idx) {
        return Register.gpr(gpd, idx);
    }

    public static final Register gpq(int idx) {
        return Register.gpr(gpq, idx);
    }

    static {
        for (int i = 0; i < 16; ++i) {
            Register.gpb[i] = new Register(0 | i, 1);
            Register.gpw[i] = new Register(0x10 | i, 2);
            Register.gpd[i] = new Register(0x20 | i, 4);
            Register.gpq[i] = new Register(0x30 | i, 8);
        }
    }
}

