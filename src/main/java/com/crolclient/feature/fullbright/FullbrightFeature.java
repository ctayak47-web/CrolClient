package com.crolclient.feature.fullbright;
import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
public class FullbrightFeature extends Feature {
    public FullbrightFeature() {
        super("Fullbright", "Maximum brightness without night vision", FeatureCategory.VISUAL);
    }
    @Override
    protected void onEnable() {
        ConfigManager.getConfig().fullbrightEnabled = true;
        ConfigManager.save();
    }
    @Override
    protected void onDisable() {
        ConfigManager.getConfig().fullbrightEnabled = false;
        ConfigManager.save();
    }
}
