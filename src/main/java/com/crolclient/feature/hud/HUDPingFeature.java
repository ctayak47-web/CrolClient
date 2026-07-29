package com.crolclient.feature.hud;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import com.crolclient.hud.HUDManager;

public class HUDPingFeature extends Feature {
    public HUDPingFeature() {
        super("Ping", "Display server ping", FeatureCategory.HUD);
        this.enabled = ConfigManager.getConfig().hudPingEnabled;
    }

    @Override
    protected void onEnable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("Ping"))
            .findFirst().ifPresent(e -> e.setEnabled(true));
        ConfigManager.getConfig().hudPingEnabled = true;
        ConfigManager.save();
    }

    @Override
    protected void onDisable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("Ping"))
            .findFirst().ifPresent(e -> e.setEnabled(false));
        ConfigManager.getConfig().hudPingEnabled = false;
        ConfigManager.save();
    }
}
