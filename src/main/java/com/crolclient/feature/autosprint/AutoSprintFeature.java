package com.crolclient.feature.autosprint;
import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
public class AutoSprintFeature extends Feature {
    public AutoSprintFeature() {
        super("Auto Sprint", "Automatically sprint when moving", FeatureCategory.MOVEMENT);
    }
    @Override
    protected void onEnable() {
        ConfigManager.getConfig().autoSprintEnabled = true;
        ConfigManager.save();
    }
    @Override
    protected void onDisable() {
        ConfigManager.getConfig().autoSprintEnabled = false;
        ConfigManager.save();
    }
}
