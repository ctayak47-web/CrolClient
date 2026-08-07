
package jnr.a64asm;

import jnr.a64asm.Ext;
import jnr.a64asm.Label;
import jnr.a64asm.Operand;
import jnr.a64asm.Register;

public class Mem
extends Operand {
    private final int base;
    private final int index;
    private final int shift;
    private final Ext extend;
    private final Label label;
    private final long target;
    private final long displacement;

    Mem(Register base, int size) {
        this(base.index(), 255, 0, null, 0L, 0L, size, null);
    }

    Mem(Register base, Ext extend, int size) {
        this(base.index(), 255, 0, null, 0L, 0L, size, null);
    }

    Mem(Label label, long displacement, int size) {
        this(255, 255, 0, label, 0L, displacement, size, null);
    }

    Mem(Register base, long displacement, int size) {
        this(base.index(), 255, 0, null, 0L, displacement, size, null);
    }

    Mem(Register base, Register index, int shift, long displacement, int size) {
        this(base.index(), index.index(), shift, null, 0L, displacement, size, null);
    }

    Mem(Label label, Register index, int shift, long disp, int ptrSize) {
        this(0, index.index(), shift, label, 0L, disp, ptrSize, null);
    }

    Mem(long target, long disp, int ptrSize) {
        this(255, 255, 0, null, target, disp, ptrSize, null);
    }

    Mem(long target, Register index, int shift, long disp, int ptrSize) {
        this(255, index.index(), shift, null, target, disp, ptrSize, null);
    }

    private Mem(int base, int index, int shift, Label label, long target, long displacement, int size, Ext extend) {
        super(2, size);
        assert (shift <= 3);
        this.base = base;
        this.index = index;
        this.shift = shift;
        this.label = label;
        this.target = target;
        this.displacement = displacement;
        this.extend = extend;
    }

    public final boolean hasLabel() {
        return this.label != null;
    }

    public final boolean hasBase() {
        return this.base != 255;
    }

    boolean hasIndex() {
        return this.index != 255;
    }

    public final int base() {
        return this.base;
    }

    public final long displacement() {
        return this.displacement;
    }

    public final int index() {
        return this.index;
    }

    public final Label label() {
        return this.label;
    }

    public final int shift() {
        return this.shift;
    }

    public final long target() {
        return this.target;
    }
}

