package ru.crolclient.implement.events.container;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import ru.crolclient.api.event.events.Event;
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandledScreenEvent implements Event {
    DrawContext drawContext;
    int backgroundWidth;
    int backgroundHeight;
}
