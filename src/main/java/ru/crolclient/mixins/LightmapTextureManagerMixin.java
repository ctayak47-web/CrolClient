package ru.crolclient.mixins;

import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.AmbienceModule;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void getBrightnessHook(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        AmbienceModule ambienceModule = (AmbienceModule) Extra.getInstance().getModuleProvider().module("Ambience");

        if (ambienceModule != null && ambienceModule.isState()) {
            float baseLight = (float)lightLevel / 15.0F;
            float adjustedLight = baseLight / (4.0F - 3.0F * baseLight);
            float brightness = ambienceModule.getBrightnessLevel();

            cir.setReturnValue(Math.max(
                MathHelper.lerp(type.ambientLight(), adjustedLight, 1.0F),
                brightness
            ));
        }
    }
}