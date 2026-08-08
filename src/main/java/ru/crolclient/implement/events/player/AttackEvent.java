package ru.crolclient.implement.events.player;

import lombok.Value;
import net.minecraft.entity.Entity;
import ru.crolclient.api.event.events.Event;

@Value
public class AttackEvent implements Event {
    Entity entity;
    byte eventType;
}
