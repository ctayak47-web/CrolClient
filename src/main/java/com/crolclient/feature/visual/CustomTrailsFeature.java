package com.crolclient.feature.visual;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class CustomTrailsFeature extends Feature {
    public CustomTrailsFeature() {
        super("Custom Trails", "Movement trail particles", FeatureCategory.VISUAL);
        settings.add(new ModeSetting("Mode", "firefly", Arrays.asList("firefly", "jump")));
        this.enabled = ConfigManager.getConfig().customTrailsEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().customTrailsEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customTrailsEnabled = false; ConfigManager.save(); }
}
