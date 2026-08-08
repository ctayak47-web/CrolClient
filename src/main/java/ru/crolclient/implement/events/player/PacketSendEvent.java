package ru.crolclient.implement.events.player;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.packet.Packet;
import ru.crolclient.api.event.events.Event;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = false)
public class PacketSendEvent implements Event {
    Packet<?> packet;
    boolean cancelled;

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
