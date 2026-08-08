package ru.crolclient.implement.features.modules.movement;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.implement.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class TwerkModule extends Module {

    private boolean sneaking = false;

    public TwerkModule() {
        super("Twerk", "Twerk", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (mc.player != null && isState()) {
            sneaking = !sneaking;

            if (sneaking) {
                mc.options.sneakKey.setPressed(true);
            } else {
                mc.options.sneakKey.setPressed(false);
            }
        }
    }

    @Override
    public void deactivate() {
        if (mc.player != null) {
            mc.options.sneakKey.setPressed(false);
        }
        super.deactivate();
    }
}
