
package jnr.a64asm;

import java.util.LinkedList;
import java.util.List;
import jnr.a64asm.LABEL_STATE;
import jnr.a64asm.LinkData;
import jnr.a64asm.Operand;

public final class Label
extends Operand {
    final int id;
    LABEL_STATE state;
    int position;
    final List<LinkData> links = new LinkedList<LinkData>();

    public Label() {
        this(0);
    }

    public Label(int id) {
        super(4, 4);
        this.id = id;
        this.state = LABEL_STATE.LABEL_STATE_UNUSED;
        this.position = -1;
    }

    final boolean isUnused() {
        return this.state == LABEL_STATE.LABEL_STATE_UNUSED;
    }

    final boolean isLinked() {
        return this.state == LABEL_STATE.LABEL_STATE_LINKED;
    }

    final boolean isBound() {
        return this.state == LABEL_STATE.LABEL_STATE_BOUND;
    }

    final int position() {
        return this.position;
    }

    final void link(LinkData link) {
        this.links.add(link);
        this.state = LABEL_STATE.LABEL_STATE_LINKED;
    }
}

