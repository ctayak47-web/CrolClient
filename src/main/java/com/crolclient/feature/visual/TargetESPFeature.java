package com.crolclient.feature.visual;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class TargetESPFeature extends Feature {
    public TargetESPFeature() {
        super("Target ESP", "Highlights targeted entity", FeatureCategory.VISUAL);
        settings.add(new ModeSetting("Mode", "Box", Arrays.asList("Box", "Chain", "Circle")));
        this.enabled = ConfigManager.getConfig().targetEspEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().targetEspEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().targetEspEnabled = false; ConfigManager.save(); }
}
