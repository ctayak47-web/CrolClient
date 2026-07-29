package com.crolclient.feature.player;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class CustomArrowFeature extends Feature {
    public CustomArrowFeature() {
        super("Custom Arrows", "Replace arrow texture", FeatureCategory.PLAYER);
        settings.add(new ModeSetting("Mode", "default", Arrays.asList("default")));
        this.enabled = ConfigManager.getConfig().customArrowEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().customArrowEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customArrowEnabled = false; ConfigManager.save(); }
}
