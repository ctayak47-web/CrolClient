package ru.crolclient.mixins;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.api.system.animation.implement.FastAnimation;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.RotationPlan;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.Angle;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.RotationController;
import ru.crolclient.implement.features.modules.render.CustomCameraModule;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER))
    private void injectQuickPerspectiveSwap(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Camera self = (Camera) (Object) this;
        RotationController rotationController = RotationController.INSTANCE;
        RotationPlan rotationPlan = rotationController.getCurrentRotationPlan();
        Angle previousAngle = rotationController.getPreviousAngle();
        Angle currentAngle = rotationController.getCurrentAngle();

        boolean shouldModifyRotation = rotationPlan != null && rotationPlan.isChangeLook();

        if (currentAngle == null || previousAngle == null || !shouldModifyRotation) {
            return;
        }

        try {
            var setRotationMethod = Camera.class.getDeclaredMethod("setRotation", float.class, float.class);
            setRotationMethod.setAccessible(true);
            setRotationMethod.invoke(self,
                    MathHelper.lerp(tickDelta, previousAngle.getYaw(), currentAngle.getYaw()),
                    MathHelper.lerp(tickDelta, previousAngle.getPitch(), currentAngle.getPitch())
            );
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void updateAnimation(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        CustomCameraModule customCamera = (CustomCameraModule) Extra.getInstance()
                .getModuleProvider().module("CustomCamera");

        if (customCamera != null && customCamera.isState()) {
            FastAnimation animation = customCamera.getCameraAnimation();
            if (thirdPerson != (animation.getCurrent() > 0.5f)) {
                animation.setDirection(FastAnimation.getDirection(thirdPerson));
            }
        }
    }
}
