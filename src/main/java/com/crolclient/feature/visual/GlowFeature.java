package com.crolclient.feature.visual;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class GlowFeature extends Feature {
    public GlowFeature() {
        super("Glow", "Entity glow effect overlay", FeatureCategory.VISUAL);
        settings.add(new ModeSetting("Mode", "glow", Arrays.asList("glow", "bloom", "dashbloom")));
        this.enabled = ConfigManager.getConfig().customGlowEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().customGlowEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customGlowEnabled = false; ConfigManager.save(); }
}
