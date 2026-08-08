package ru.crolclient.implement.events.render;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.util.math.MatrixStack;
import ru.crolclient.api.event.events.Event;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class Render2DEvent implements Event {
    MatrixStack matrixStack;
    float tickDelta;
    int screenWidth;
    int screenHeight;
}