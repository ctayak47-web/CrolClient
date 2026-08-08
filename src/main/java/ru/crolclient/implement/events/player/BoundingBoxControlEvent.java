package ru.crolclient.implement.events.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import ru.crolclient.api.event.events.Event;
import ru.crolclient.api.event.events.callables.EventCancellable;


@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoundingBoxControlEvent extends EventCancellable {
    Box box;
    Box changedBox;
    Entity entity;
    public BoundingBoxControlEvent(Box box, Entity entity) {
        this.box = box;
        this.entity = entity;
    }
}
