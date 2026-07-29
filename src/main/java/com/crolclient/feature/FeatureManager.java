package com.crolclient.feature;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.autoeat.AutoEatFeature;
import com.crolclient.feature.autosprint.AutoSprintFeature;
import com.crolclient.feature.combat.CustomHitSoundFeature;
import com.crolclient.feature.fullbright.FullbrightFeature;
import com.crolclient.feature.hud.*;
import com.crolclient.feature.player.*;
import com.crolclient.feature.render.*;
import com.crolclient.feature.sound.*;
import com.crolclient.feature.visual.*;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class FeatureManager {
    private static final List<Feature> features = new ArrayList<>();

    public static void init() {
        // Existing
        features.add(new FullbrightFeature());
        features.add(new AutoSprintFeature());
        features.add(new AutoEatFeature());

        // HUD
        features.add(new HUDWatermarkFeature());
        features.add(new HUDArrayListFeature());
        features.add(new HUDCoordsFeature());
        features.add(new HUDFPSFeature());
        features.add(new HUDPingFeature());

        // Visual
        features.add(new TargetESPFeature());
        features.add(new CustomParticlesFeature());
        features.add(new CustomSkyFeature());
        features.add(new CustomCapeFeature());
        features.add(new CustomBackgroundFeature());
        features.add(new GlowFeature());
        features.add(new CustomTrailsFeature());
        features.add(new ViewModelFeature());
        features.add(new SwingAnimationFeature());

        // Combat
        features.add(new CustomHitSoundFeature());

        // Player
        features.add(new CustomDeathSoundFeature());
        features.add(new CustomSkinFeature());
        features.add(new CustomArrowFeature());
        features.add(new JumpEffectFeature());

        // Sound
        features.add(new UISoundsFeature());
        features.add(new LowHPSoundFeature());

        // Load config states
        for (Feature f : features) {
            if (f instanceof FullbrightFeature && ConfigManager.getConfig().fullbrightEnabled) f.setEnabled(true);
            if (f instanceof AutoSprintFeature && ConfigManager.getConfig().autoSprintEnabled) f.setEnabled(true);
            if (f instanceof AutoEatFeature && ConfigManager.getConfig().autoEatEnabled) f.setEnabled(true);
            if (f instanceof TargetESPFeature && ConfigManager.getConfig().targetEspEnabled) f.setEnabled(true);
            if (f instanceof CustomParticlesFeature && ConfigManager.getConfig().customParticlesEnabled) f.setEnabled(true);
            if (f instanceof CustomSkyFeature && ConfigManager.getConfig().customSkyEnabled) f.setEnabled(true);
            if (f instanceof CustomCapeFeature && ConfigManager.getConfig().customCapeEnabled) f.setEnabled(true);
            if (f instanceof CustomBackgroundFeature && ConfigManager.getConfig().customBackgroundEnabled) f.setEnabled(true);
            if (f instanceof GlowFeature && ConfigManager.getConfig().customGlowEnabled) f.setEnabled(true);
            if (f instanceof CustomTrailsFeature && ConfigManager.getConfig().customTrailsEnabled) f.setEnabled(true);
        if (f instanceof ViewModelFeature && ConfigManager.getConfig().viewmodelEnabled) f.setEnabled(true);
        if (f instanceof SwingAnimationFeature && ConfigManager.getConfig().swingAnimationEnabled) f.setEnabled(true);
            if (f instanceof CustomHitSoundFeature && ConfigManager.getConfig().customHitSoundEnabled) f.setEnabled(true);
            if (f instanceof CustomDeathSoundFeature && ConfigManager.getConfig().customDeathSoundEnabled) f.setEnabled(true);
            if (f instanceof CustomSkinFeature && ConfigManager.getConfig().customSkinEnabled) f.setEnabled(true);
            if (f instanceof CustomArrowFeature && ConfigManager.getConfig().customArrowEnabled) f.setEnabled(true);
            if (f instanceof JumpEffectFeature && ConfigManager.getConfig().jumpEffectEnabled) f.setEnabled(true);
            if (f instanceof UISoundsFeature && ConfigManager.getConfig().uiSoundsEnabled) f.setEnabled(true);
            if (f instanceof LowHPSoundFeature && ConfigManager.getConfig().lowHpSoundEnabled) f.setEnabled(true);
        }
    }

    public static List<Feature> getFeatures() { return features; }

    public static List<Feature> getFeaturesByCategory(FeatureCategory category) {
        List<Feature> result = new ArrayList<>();
        for (Feature f : features) if (f.getCategory() == category) result.add(f);
        return result;
    }

    public static void onTick(MinecraftClient client) {
        for (Feature f : features) if (f.isEnabled()) f.onTick();
    }
}
