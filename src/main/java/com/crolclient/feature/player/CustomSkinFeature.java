package com.crolclient.feature.player;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class CustomSkinFeature extends Feature {
    public CustomSkinFeature() {
        super("Custom Skin", "Overlay custom skin texture", FeatureCategory.PLAYER);
        settings.add(new ModeSetting("Mode", "amogus", Arrays.asList("amogus", "demon", "jeff", "rabbit")));
        this.enabled = ConfigManager.getConfig().customSkinEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().customSkinEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customSkinEnabled = false; ConfigManager.save(); }
}
