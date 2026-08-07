
package com.kenai.jnr.x86asm;

import com.kenai.jnr.x86asm.BaseReg;

@Deprecated
public final class XMMRegister
extends BaseReg {
    static final XMMRegister[] cache = new XMMRegister[16];

    private XMMRegister(int code, int size) {
        super(code, size);
    }

    public static final XMMRegister xmm(int idx) {
        if (idx >= 0 && idx < cache.length) {
            return cache[idx];
        }
        throw new IllegalArgumentException("invalid xmm register");
    }

    static {
        for (int i = 0; i < cache.length; ++i) {
            XMMRegister.cache[i] = new XMMRegister(0x70 | i, 16);
        }
    }
}

