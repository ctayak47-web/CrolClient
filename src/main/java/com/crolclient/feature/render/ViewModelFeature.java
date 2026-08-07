package com.crolclient.feature.render;
import com.crolclient.config.ConfigManager;
import com.crolclient.config.setting.FloatSetting;
import com.crolclient.config.setting.ModeSetting;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import java.util.Arrays;
public class ViewModelFeature extends Feature {
    public ViewModelFeature() {
        super("ViewModel", "Custom first-person hand position/rotation", FeatureCategory.VISUAL);
        settings.add(new FloatSetting("Pos X", 0.0f, -3.0f, 3.0f, 0.01f));
        settings.add(new FloatSetting("Pos Y", 0.0f, -3.0f, 3.0f, 0.01f));
        settings.add(new FloatSetting("Pos Z", 0.0f, -3.0f, 3.0f, 0.01f));
        settings.add(new FloatSetting("Rot X", 0.0f, -180.0f, 180.0f, 1.0f));
        settings.add(new FloatSetting("Rot Y", 0.0f, -180.0f, 180.0f, 1.0f));
        settings.add(new FloatSetting("Rot Z", 0.0f, -180.0f, 180.0f, 1.0f));
        settings.add(new FloatSetting("Scale", 1.0f, 0.1f, 2.0f, 0.05f));
        this.enabled = ConfigManager.getConfig().viewmodelEnabled;
    }
    @Override protected void onEnable() { ConfigManager.getConfig().viewmodelEnabled = true; ConfigManager.save(); }
    @Override protected void onDisable() { ConfigManager.getConfig().viewmodelEnabled = false; ConfigManager.save(); }
    public static float getPosX() {
        return ConfigManager.getConfig().viewmodelPosX;
    }
    public static float getPosY() {
        return ConfigManager.getConfig().viewmodelPosY;
    }
    public static float getPosZ() {
        return ConfigManager.getConfig().viewmodelPosZ;
    }
    public static float getRotX() {
        return ConfigManager.getConfig().viewmodelRotX;
    }
    public static float getRotY() {
        return ConfigManager.getConfig().viewmodelRotY;
    }
    public static float getRotZ() {
        return ConfigManager.getConfig().viewmodelRotZ;
    }
    public static float getScale() {
        return ConfigManager.getConfig().viewmodelScale;
    }
    public static void updateFromSettings(ViewModelFeature f) {
        ConfigManager.getConfig().viewmodelPosX = ((FloatSetting)f.getSettings().get(0)).getValue();
        ConfigManager.getConfig().viewmodelPosY = ((FloatSetting)f.getSettings().get(1)).getValue();
        ConfigManager.getConfig().viewmodelPosZ = ((FloatSetting)f.getSettings().get(2)).getValue();
        ConfigManager.getConfig().viewmodelRotX = ((FloatSetting)f.getSettings().get(3)).getValue();
        ConfigManager.getConfig().viewmodelRotY = ((FloatSetting)f.getSettings().get(4)).getValue();
        ConfigManager.getConfig().viewmodelRotZ = ((FloatSetting)f.getSettings().get(5)).getValue();
        ConfigManager.getConfig().viewmodelScale = ((FloatSetting)f.getSettings().get(6)).getValue();
        ConfigManager.save();
    }
}
