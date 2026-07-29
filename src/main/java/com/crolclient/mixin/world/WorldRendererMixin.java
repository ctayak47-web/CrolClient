package com.crolclient.mixin.world;

import com.crolclient.config.ConfigManager;
import com.crolclient.render.sky.CustomSkyRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Runnable fogCallback, CallbackInfo ci) {
        if (ConfigManager.getConfig().customSkyEnabled) {
            // CustomSkyRenderer.renderSky(...); // Uncomment when fully implemented
            // ci.cancel(); // Cancel vanilla sky if needed
        }
    }
}
