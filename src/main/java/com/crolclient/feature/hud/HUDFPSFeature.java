package com.crolclient.feature.hud;
import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import com.crolclient.hud.HUDManager;
public class HUDFPSFeature extends Feature {
    public HUDFPSFeature() {
        super("FPS Counter", "Display FPS on screen", FeatureCategory.HUD);
        this.enabled = ConfigManager.getConfig().hudFpsEnabled;
    }
    @Override
    protected void onEnable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("FPS"))
            .findFirst().ifPresent(e -> e.setEnabled(true));
        ConfigManager.getConfig().hudFpsEnabled = true;
        ConfigManager.save();
    }
    @Override
    protected void onDisable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("FPS"))
            .findFirst().ifPresent(e -> e.setEnabled(false));
        ConfigManager.getConfig().hudFpsEnabled = false;
        ConfigManager.save();
    }
}
