package com.crolclient.feature.sound;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

public class LowHPSoundFeature extends Feature {
    public LowHPSoundFeature() {
        super("Low HP Sound", "Warning sound on low health", FeatureCategory.SOUND);
        this.enabled = ConfigManager.getConfig().lowHpSoundEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().lowHpSoundEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().lowHpSoundEnabled = false; ConfigManager.save(); }
}
