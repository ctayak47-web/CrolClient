package com.crolclient.feature.render;

import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;

import java.util.Arrays;

public class SwingAnimationFeature extends Feature {
    public SwingAnimationFeature() {
        super("Swing Animation", "Custom hand swing easing", FeatureCategory.VISUAL);
        settings.add(new ModeSetting("Mode", "1.7", Arrays.asList("1.7", "Smooth", "Old", "Expo")));
        this.enabled = ConfigManager.getConfig().swingAnimationEnabled;
    }

    @Override protected void onEnable() { ConfigManager.getConfig().swingAnimationEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().swingAnimationEnabled = false; ConfigManager.save(); }

    public static float applyEasing(float progress) {
        String mode = ConfigManager.getConfig().swingAnimationMode;
        return switch (mode) {
            case "1.7" -> progress; // Linear like 1.7
            case "Smooth" -> progress * progress * (3 - 2 * progress); // Smoothstep
            case "Old" -> (float)Math.sin(progress * Math.PI); // Sine
            case "Expo" -> progress < 0.5f ? (float)Math.pow(2, 20 * progress - 10) / 2 : (2 - (float)Math.pow(2, -20 * progress + 10)) / 2;
            default -> progress;
        };
    }
}
