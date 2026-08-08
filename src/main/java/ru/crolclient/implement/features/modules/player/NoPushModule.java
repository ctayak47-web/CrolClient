package ru.crolclient.implement.features.modules.player;

import com.google.common.eventbus.Subscribe;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.implement.events.block.PushBlockEvent;
import ru.crolclient.implement.events.block.PushPlayerEvent;
import ru.crolclient.implement.events.block.PushWaterEvent;
import ru.crolclient.implement.events.block.ShouldBlockVision;

public class NoPushModule extends Module {
    public NoPushModule() {
        super("NoPush", "No Push", ModuleCategory.PLAYER);
    }
    @EventHandler
    public void onPushPlayer(PushPlayerEvent pushPlayerEvent) {
        pushPlayerEvent.cancel();
    }
    @EventHandler
    public void onPushBlock(PushBlockEvent pushBlockEvent) {
        pushBlockEvent.cancel();
    }

    @EventHandler
    public void onBlockVision(ShouldBlockVision pushWaterEvent) {
        pushWaterEvent.cancel();
    }

    @EventHandler
    public void onPushWater(PushWaterEvent pushWaterEvent) {
        pushWaterEvent.cancel();
    }
}
