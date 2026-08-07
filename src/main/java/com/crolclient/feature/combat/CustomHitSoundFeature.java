package com.crolclient.feature.combat;
import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import java.util.Arrays;
public class CustomHitSoundFeature extends Feature {
    public CustomHitSoundFeature() {
        super("Custom Hit Sounds", "Replace hit sounds", FeatureCategory.COMBAT);
        settings.add(new ModeSetting("Mode", "hit1", Arrays.asList("hit1", "hit2", "hit3", "bell", "bonk", "bubble", "pop", "uwu", "moan1", "moan2", "moan3", "moan4")));
        this.enabled = ConfigManager.getConfig().customHitSoundEnabled;
    }
    @Override protected void onEnable() { ConfigManager.getConfig().customHitSoundEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().customHitSoundEnabled = false; ConfigManager.save(); }
}
