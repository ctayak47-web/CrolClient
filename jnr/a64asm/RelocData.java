
package jnr.a64asm;

class RelocData {
    final Type type;
    final int size;
    final int offset;
    final long destination;

    public RelocData(Type type, int size, int offset, long destination) {
        this.type = type;
        this.size = size;
        this.offset = offset;
        this.destination = destination;
    }

    static enum Type {
        ABSOLUTE_TO_ABSOLUTE,
        RELATIVE_TO_ABSOLUTE,
        ABSOLUTE_TO_RELATIVE,
        ABSOLUTE_TO_RELATIVE_TRAMPOLINE;

    }
}

