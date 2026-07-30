package com.crolclient.mixin.world;

import com.crolclient.config.ConfigManager;
import com.crolclient.render.sky.CustomSkyRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    // В 1.21.4 сигнатура renderSky изменилась:
    // было:  renderSky(Matrix4f, Matrix4f, float, Runnable)
    // стало: renderSky(FrameGraphBuilder, Camera, float, Fog)  (метод приватный)
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        if (ConfigManager.getConfig().customSkyEnabled) {
            // CustomSkyRenderer.renderSky(...); // Uncomment when fully implemented
            // ci.cancel(); // Cancel vanilla sky if needed
        }
    }
}
