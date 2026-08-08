
package crol.client.base.events.impl.server;

import lombok.Generated;
import net.minecraft.Packet;
import crol.client.base.events.callables.EventCancellable;

public class EventPacket
extends EventCancellable {
    private final Action action;
    private Packet<?> packet;

    public boolean isSent() {
        return this.getAction() == Action.SENT;
    }

    public boolean isReceive() {
        return this.getAction() == Action.RECEIVE;
    }

    @Generated
    public Action getAction() {
        return this.action;
    }

    @Generated
    public Packet<?> getPacket() {
        return this.packet;
    }

    @Generated
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    @Generated
    public EventPacket(Action action, Packet<?> packet) {
        this.action = action;
        this.packet = packet;
    }

    public static enum Action {
        SENT,
        RECEIVE;

    }
}

