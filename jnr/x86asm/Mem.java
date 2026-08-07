
package jnr.x86asm;

import jnr.x86asm.Label;
import jnr.x86asm.Operand;
import jnr.x86asm.Register;
import jnr.x86asm.SEGMENT;

public final class Mem
extends Operand {
    private final int base;
    private final int index;
    private final int shift;
    private final SEGMENT segmentPrefix;
    private final Label label;
    private final long target;
    private final long displacement;

    Mem(Label label, long displacement, int size) {
        this(255, 255, 0, SEGMENT.SEGMENT_NONE, label, 0L, displacement, size);
    }

    Mem(Register base, long displacement, int size) {
        this(base.index(), 255, 0, SEGMENT.SEGMENT_NONE, null, 0L, displacement, size);
    }

    Mem(Register base, Register index, int shift, long displacement, int size) {
        this(base.index(), index.index(), shift, SEGMENT.SEGMENT_NONE, null, 0L, displacement, size);
    }

    Mem(Label label, Register index, int shift, long disp, int ptrSize) {
        this(0, index.index(), shift, SEGMENT.SEGMENT_NONE, label, 0L, disp, ptrSize);
    }

    Mem(long target, long disp, SEGMENT segmentPrefix, int ptrSize) {
        this(255, 255, 0, segmentPrefix, null, target, disp, ptrSize);
    }

    Mem(long target, Register index, int shift, SEGMENT segmentPrefix, long disp, int ptrSize) {
        this(255, index.index(), shift, segmentPrefix, null, target, disp, ptrSize);
    }

    private Mem(int base, int index, int shift, SEGMENT segmentPrefix, Label label, long target, long displacement, int size) {
        super(2, size);
        assert (shift <= 3);
        this.base = base;
        this.index = index;
        this.shift = shift;
        this.segmentPrefix = segmentPrefix;
        this.label = label;
        this.target = target;
        this.displacement = displacement;
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

    public final SEGMENT segmentPrefix() {
        return this.segmentPrefix;
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

