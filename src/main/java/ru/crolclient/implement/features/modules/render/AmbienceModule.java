package ru.crolclient.implement.features.modules.render;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.*;
import ru.crolclient.implement.events.packet.PacketEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class AmbienceModule extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    private final GroupSetting brightnessSettings = new GroupSetting("Brightness", "Settings for world brightness")
            .settings(
                    new ValueSetting("Level", "Adjust world brightness level")
                            .setValue(1.0F)
                            .range(0.0F, 1.0F)
                            .increment(0.1F)
            );

    private final ValueSetting timeSetting = new ValueSetting("Time", "Changes the world time")
            .setValue(6000f)
            .range(0f, 24000f)
            .increment(200f);

    private final SelectSetting weatherSetting = new SelectSetting("Weather", "Changes the weather condition")
            .value("Clear", "Rain", "Thunder");

    public AmbienceModule() {
        super("Ambience", ModuleCategory.RENDER);
        setup(brightnessSettings, timeSetting, weatherSetting);
    }

    public float getBrightnessLevel() {
        if (!brightnessSettings.isValue()) return 0.0F;
        return ((ValueSetting) brightnessSettings.getSubSetting("Level")).getValue();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (mc == null || mc.world == null) return;

        if (event.isReceive() && event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            event.cancel();

            switch (weatherSetting.getSelected()) {
                case "Clear" -> {
                    mc.world.setRainGradient(0);
                    mc.world.setThunderGradient(0);
                }
                case "Rain" -> {
                    mc.world.setRainGradient(1);
                    mc.world.setThunderGradient(0);
                }
                case "Thunder" -> {
                    mc.world.setRainGradient(1);
                    mc.world.setThunderGradient(1);
                }
            }
        }
    }
}