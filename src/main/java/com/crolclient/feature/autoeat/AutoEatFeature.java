package com.crolclient.feature.autoeat;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

public class AutoEatFeature extends Feature {
    public AutoEatFeature() {
        super("Auto Eat", "Automatically eat when hungry", FeatureCategory.UTIL);
    }

    @Override
    protected void onEnable() {
        ConfigManager.getConfig().autoEatEnabled = true;
        ConfigManager.save();
    }

    @Override
    protected void onDisable() {
        ConfigManager.getConfig().autoEatEnabled = false;
        ConfigManager.save();
    }
}
