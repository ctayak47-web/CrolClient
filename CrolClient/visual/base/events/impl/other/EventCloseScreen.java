
package crol.client.base.events.impl.other;

import lombok.Generated;
import net.minecraft.Screen;
import crol.client.base.events.callables.EventCancellable;

public class EventCloseScreen
extends EventCancellable {
    private final Screen screen;

    @Generated
    public EventCloseScreen(Screen screen) {
        this.screen = screen;
    }
}

