
package jnr.x86asm;

final class LinkData {
    final int offset;
    long displacement;
    int relocId;

    public LinkData(int offset, long displacement, int relocId) {
        this.offset = offset;
        this.displacement = displacement;
        this.relocId = relocId;
    }
}

