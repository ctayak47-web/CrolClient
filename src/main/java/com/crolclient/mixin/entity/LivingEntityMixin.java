package com.crolclient.mixin.entity;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.render.SwingAnimationFeature;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getHandSwingProgress", at = @At("RETURN"), cancellable = true)
    private void onGetHandSwingProgress(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (ConfigManager.getConfig().swingAnimationEnabled) {
            float original = cir.getReturnValue();
            cir.setReturnValue(SwingAnimationFeature.applyEasing(original));
        }
    }
}
