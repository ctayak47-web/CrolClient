package com.crolclient.feature.visual;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class CustomBackgroundFeature extends Feature {
    public CustomBackgroundFeature() {
        super("Custom Background", "Custom main menu background", FeatureCategory.VISUAL);
        settings.add(new ModeSetting("Mode", "1", Arrays.asList("1", "2", "3", "4", "5")));
        this.enabled = ConfigManager.getConfig().customBackgroundEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().customBackgroundEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customBackgroundEnabled = false; ConfigManager.save(); }
}
