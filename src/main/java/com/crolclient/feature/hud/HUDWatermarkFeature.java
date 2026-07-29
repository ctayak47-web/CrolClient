package com.crolclient.feature.hud;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import com.crolclient.hud.HUDManager;

public class HUDWatermarkFeature extends Feature {
    public HUDWatermarkFeature() {
        super("Watermark", "Display CrolClient watermark on HUD", FeatureCategory.HUD);
        this.enabled = ConfigManager.getConfig().hudWatermarkEnabled;
    }

    @Override
    protected void onEnable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("Watermark"))
            .findFirst().ifPresent(e -> e.setEnabled(true));
        ConfigManager.getConfig().hudWatermarkEnabled = true;
        ConfigManager.save();
    }

    @Override
    protected void onDisable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("Watermark"))
            .findFirst().ifPresent(e -> e.setEnabled(false));
        ConfigManager.getConfig().hudWatermarkEnabled = false;
        ConfigManager.save();
    }
}
