package com.crolclient.feature.visual;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class CustomSkyFeature extends Feature {
    public CustomSkyFeature() {
        super("Custom Sky", "Replaces skybox textures", FeatureCategory.VISUAL);
        settings.add(new ModeSetting("Mode", "sky", Arrays.asList("sky", "sky2", "sky3", "sky4")));
        this.enabled = ConfigManager.getConfig().customSkyEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().customSkyEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customSkyEnabled = false; ConfigManager.save(); }
}
