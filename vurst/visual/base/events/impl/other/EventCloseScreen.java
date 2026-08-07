
package vurst.visual.base.events.impl.other;

import lombok.Generated;
import net.minecraft.Screen;
import vurst.visual.base.events.callables.EventCancellable;

public class EventCloseScreen
extends EventCancellable {
    private final Screen screen;

    @Generated
    public EventCloseScreen(Screen screen) {
        this.screen = screen;
    }
}

