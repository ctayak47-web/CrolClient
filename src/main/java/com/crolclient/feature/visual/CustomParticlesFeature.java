package com.crolclient.feature.visual;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class CustomParticlesFeature extends Feature {
    public CustomParticlesFeature() {
        super("Custom Particles", "Custom damage/crit particles", FeatureCategory.VISUAL);
        settings.add(new ModeSetting("Mode", "heart", Arrays.asList("heart", "star", "dollar", "genshin", "rhombus", "triangle")));
        this.enabled = ConfigManager.getConfig().customParticlesEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().customParticlesEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customParticlesEnabled = false; ConfigManager.save(); }
}
