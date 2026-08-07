package com.crolclient.feature.player;
import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import java.util.Arrays;
public class CustomDeathSoundFeature extends Feature {
    public CustomDeathSoundFeature() {
        super("Custom Death Sounds", "Replace death sound", FeatureCategory.PLAYER);
        settings.add(new ModeSetting("Mode", "death", Arrays.asList("death", "est", "tank")));
        this.enabled = ConfigManager.getConfig().customDeathSoundEnabled;
    }
    @Override protected void onEnable() { ConfigManager.getConfig().customDeathSoundEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customDeathSoundEnabled = false; ConfigManager.save(); }
}
