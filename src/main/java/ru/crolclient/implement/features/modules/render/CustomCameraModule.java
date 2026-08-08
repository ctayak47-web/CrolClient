package ru.crolclient.implement.features.modules.render;

import ru.crolclient.api.system.animation.implement.FastAnimation;
import lombok.Getter;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.feature.module.setting.implement.GroupSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;

@Getter
public class CustomCameraModule extends Module {
    private final FastAnimation cameraAnimation = new FastAnimation(2, true, 1f);

    private final BooleanSetting noClip = new BooleanSetting("NoClip", "Removes camera collision with blocks")
            .setValue(false);

    private final GroupSetting distance = new GroupSetting("Distance", "Adjusts camera distance")
            .setValue(false)
            .settings(
                    new ValueSetting("Distance", "").setValue(4.0f).range(1.0f, 20.0f).increment(0.5f)
            );

    public CustomCameraModule() {
        super("CustomCamera", "Custom Camera", ModuleCategory.RENDER);
        setup(noClip, distance);
    }

    public float getAnimatedDistance() {
        if (!distance.isValue()) return 4.0f;
        ValueSetting distanceSetting = (ValueSetting) distance.getSubSetting("Distance");
        float targetDistance = distanceSetting != null ? (float) distanceSetting.getValue() : 4.0f;
        return 1.0f + ((targetDistance - 1.0f) * cameraAnimation.getOutput().floatValue());
    }
}