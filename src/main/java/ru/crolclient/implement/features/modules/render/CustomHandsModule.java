package ru.crolclient.implement.features.modules.render;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.*;
import ru.crolclient.implement.events.player.HeldItemRendererEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class CustomHandsModule extends Module {

    private final GroupSetting mainHand = new GroupSetting("MainHand", "Main hand position settings")
            .settings(
                    new ValueSetting("X", "X position").range(-2.0f, 2.0f).increment(0.1f),
                    new ValueSetting("Y", "Y position").range(-2.0f, 2.0f).increment(0.1f),
                    new ValueSetting("Z", "Z position").range(-2.0f, 2.0f).increment(0.1f)
            );

    private final GroupSetting offHand = new GroupSetting("OffHand", "Off hand position settings")
            .settings(
                    new ValueSetting("X", "X position").range(-2.0f, 2.0f).increment(0.1f),
                    new ValueSetting("Y", "Y position").range(-2.0f, 2.0f).increment(0.1f),
                    new ValueSetting("Z", "Z position").range(-2.0f, 2.0f).increment(0.1f)
            );

    private final GroupSetting animation = new GroupSetting("Animation", "Combat animation settings")
            .settings(
                    new SelectSetting("Type", "Animation type").value("Combat", "Smooth"),
                    new ValueSetting("Speed", "Animation speed").range(0.1f, 2.0f).increment(0.1f).setValue(1.0f),
                    new ValueSetting("Strength", "Animation impact").range(0.1f, 2.0f).increment(0.1f).setValue(1.0f)
            );

    public CustomHandsModule() {
        super("CustomHands", "CustomHands", ModuleCategory.RENDER);
        setup(mainHand, offHand, animation);
    }

    @EventHandler
    public void onHeldItemRender(HeldItemRendererEvent event) {
        if (!isState()) return;

        if (event.getHand() == Hand.MAIN_HAND) {
            applyMainHandTransforms(event);
        } else {
            applyOffHandTransforms(event);
        }
    }

    private void applyMainHandTransforms(HeldItemRendererEvent event) {
        if (animation.isValue()) {
            SelectSetting type = (SelectSetting)animation.getSubSetting("Type");
            if (type.isSelected("Combat")) {
                applyCombatAnimation(event);
            } else if (type.isSelected("Smooth")) {
                applySmoothAnimation(event);
            }
        }

        if (mainHand.isValue()) {
            applyPositionSettings(event, mainHand);
        }
    }

    private void applyOffHandTransforms(HeldItemRendererEvent event) {
        if (offHand.isValue()) {
            applyPositionSettings(event, offHand);
        }
    }

    private void applyCombatAnimation(HeldItemRendererEvent event) {
        float speed = ((ValueSetting)animation.getSubSetting("Speed")).getValue();
        float strength = ((ValueSetting)animation.getSubSetting("Strength")).getValue();
        float progress = event.getSwingProgress() * speed;
        float swing = MathHelper.sin(MathHelper.sqrt(progress) * (float)Math.PI) * strength;

        event.getMatrix().multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0f * swing));
        event.getMatrix().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-25.0f * (1.0f - swing)));
        event.getMatrix().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(60.0f * swing));
    }

    private void applySmoothAnimation(HeldItemRendererEvent event) {
        float speed = ((ValueSetting)animation.getSubSetting("Speed")).getValue();
        float strength = ((ValueSetting)animation.getSubSetting("Strength")).getValue();
        float progress = event.getSwingProgress() * speed;
        float swing = MathHelper.sin(progress * (float)Math.PI) * strength;

        event.getMatrix().multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0f * swing));
        event.getMatrix().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-10.0f * swing));
        event.getMatrix().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(40.0f * swing));
    }

    private void applyPositionSettings(HeldItemRendererEvent event, GroupSetting hand) {
        float x = ((ValueSetting)hand.getSubSetting("X")).getValue();
        float y = ((ValueSetting)hand.getSubSetting("Y")).getValue();
        float z = ((ValueSetting)hand.getSubSetting("Z")).getValue();

        event.getMatrix().translate(x, y, z);
    }
}