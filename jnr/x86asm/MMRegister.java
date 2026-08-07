
package jnr.x86asm;

import jnr.x86asm.BaseReg;

public final class MMRegister
extends BaseReg {
    static final MMRegister[] cache = new MMRegister[8];

    private MMRegister(int code, int size) {
        super(code, size);
    }

    public static final MMRegister mm(int code) {
        if (code >= 0 && code < cache.length) {
            return cache[code];
        }
        throw new IllegalArgumentException("invalid mm register");
    }

    static {
        for (int i = 0; i < cache.length; ++i) {
            MMRegister.cache[i] = new MMRegister(0x60 | i, 8);
        }
    }
}

