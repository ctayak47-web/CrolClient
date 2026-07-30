package com.crolclient.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        // Если внутри нужен tickDelta, его можно достать вот так:
        // float tickDelta = tickCounter.getTickDelta(true);
        
        // Reserved
    }
}
