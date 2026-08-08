package ru.crolclient.implement.events.render;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.event.events.Event;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class DrawOverlayObjectEvent implements Event {

    public enum OverlayType {
        FIRE_OVERLAY,
        WATER_OVERLAY,
        POWDER_SNOW_OVERLAY
    }

    OverlayType overlayType;
    boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
