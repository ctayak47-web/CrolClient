package com.crolclient.feature.hud;
import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import com.crolclient.hud.HUDManager;
public class HUDArrayListFeature extends Feature {
    public HUDArrayListFeature() {
        super("ArrayList", "Display enabled modules list", FeatureCategory.HUD);
        this.enabled = ConfigManager.getConfig().hudArraylistEnabled;
    }
    @Override
    protected void onEnable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("ArrayList"))
            .findFirst().ifPresent(e -> e.setEnabled(true));
        ConfigManager.getConfig().hudArraylistEnabled = true;
        ConfigManager.save();
    }
    @Override
    protected void onDisable() {
        HUDManager.getElements().stream()
            .filter(e -> e.getName().equals("ArrayList"))
            .findFirst().ifPresent(e -> e.setEnabled(false));
        ConfigManager.getConfig().hudArraylistEnabled = false;
        ConfigManager.save();
    }
}
