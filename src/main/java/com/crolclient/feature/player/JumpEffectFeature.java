package com.crolclient.feature.player;
import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
public class JumpEffectFeature extends Feature {
    public JumpEffectFeature() {
        super("Jump Effect", "Particle effect on jump", FeatureCategory.PLAYER);
        this.enabled = ConfigManager.getConfig().jumpEffectEnabled;
    }
    @Override protected void onEnable() { ConfigManager.getConfig().jumpEffectEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().jumpEffectEnabled = false; ConfigManager.save(); }
}
