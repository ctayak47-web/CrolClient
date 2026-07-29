package com.crolclient.feature.visual;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class CustomCapeFeature extends Feature {
    public CustomCapeFeature() {
        super("Custom Cape", "Custom player cape texture", FeatureCategory.VISUAL);
        settings.add(new ModeSetting("Mode", "cape", Arrays.asList("cape")));
        this.enabled = ConfigManager.getConfig().customCapeEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().customCapeEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customCapeEnabled = false; ConfigManager.save(); }
}
