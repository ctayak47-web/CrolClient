package ru.crolclient.implement.events.player;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import ru.crolclient.api.event.events.Event;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HeldItemRendererEvent implements Event {
    Hand hand;
    MatrixStack matrix;
    float swingProgress;
}