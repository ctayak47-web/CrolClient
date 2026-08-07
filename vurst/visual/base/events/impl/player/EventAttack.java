
package vurst.visual.base.events.impl.player;

import lombok.Generated;
import net.minecraft.Entity;
import vurst.visual.base.events.callables.EventCancellable;

public final class EventAttack
extends EventCancellable {
    private final Entity target;
    private final Action action;

    @Generated
    public Entity getTarget() {
        return this.target;
    }

    @Generated
    public Action getAction() {
        return this.action;
    }

    @Generated
    public EventAttack(Entity target, Action action) {
        this.target = target;
        this.action = action;
    }

    public static enum Action {
        POST,
        PRE;

    }
}

