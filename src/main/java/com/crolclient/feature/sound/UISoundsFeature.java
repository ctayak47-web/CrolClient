package com.crolclient.feature.sound;
import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
public class UISoundsFeature extends Feature {
    public UISoundsFeature() {
        super("UI Sounds", "Custom GUI and toggle sounds", FeatureCategory.SOUND);
        this.enabled = ConfigManager.getConfig().uiSoundsEnabled;
    }
    @Override protected void onEnable() { ConfigManager.getConfig().uiSoundsEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().uiSoundsEnabled = false; ConfigManager.save(); }
}
