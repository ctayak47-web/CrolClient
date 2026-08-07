package com.crolclient.feature.hud;
import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import com.crolclient.hud.HUDManager;
public class HUDCoordsFeature extends Feature {
    public HUDCoordsFeature() {
        super("Coordinates", "Display player coordinates", FeatureCategory.HUD);
        this.enabled = ConfigManager.getConfig().hudCoordsEnabled;
    }
    @Override
    protected void onEnable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("Coords"))
            .findFirst().ifPresent(e -> e.setEnabled(true));
        ConfigManager.getConfig().hudCoordsEnabled = true;
        ConfigManager.save();
    }
    @Override
    protected void onDisable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("Coords"))
            .findFirst().ifPresent(e -> e.setEnabled(false));
        ConfigManager.getConfig().hudCoordsEnabled = false;
        ConfigManager.save();
    }
}
