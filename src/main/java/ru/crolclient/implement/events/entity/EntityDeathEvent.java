package ru.crolclient.implement.events.entity;

import lombok.Value;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import ru.crolclient.api.event.events.Event;

@Value
public class EntityDeathEvent implements Event {
    Entity entity;
    DamageSource source;
}
