
package jnr.a64asm;

import jnr.a64asm.Immediate;
import jnr.a64asm.Operand;
import jnr.a64asm.Register;

public final class Post_index
extends Operand {
    private final Immediate postIndex;
    private final Register basereg;

    public Post_index(Register base, Immediate postIndex) {
        super(13, 0);
        this.basereg = base;
        this.postIndex = postIndex;
    }

    public final Immediate getPostIndex() {
        return this.postIndex;
    }

    public final Register getRegister() {
        return this.basereg;
    }
}

