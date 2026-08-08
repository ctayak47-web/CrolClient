package ru.crolclient.implement.features.modules.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.implement.events.player.DeathScreenEvent;

@SuppressWarnings("all")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoRespawnModule extends Module {
    ValueSetting delaySetting = new ValueSetting("Respawn delay", "Delay in ticks")
            .setValue(20)
            .range(0, 70);


    public AutoRespawnModule() {
        super("AutoRespawn", "Auto Respawn", ModuleCategory.PLAYER);
        setup(delaySetting);
    }

    @EventHandler
    public void onDeathScreen(DeathScreenEvent deathScreenEvent) {
        int ticksSinceDeath = deathScreenEvent.getTicksSinceDeath();
        int delay = Math.round(delaySetting.getValue());

        if (ticksSinceDeath > delay) {
            mc.player.requestRespawn();
            mc.setScreen(null);
        }
    }
}
