package com.crolclient.mixin.entity;

import com.crolclient.config.ConfigManager;
import net.minecraft.client.render.entity.ArrowEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowEntityRenderer.class)
public class ArrowEntityRendererMixin {
    private static final Identifier CUSTOM_ARROW = Identifier.of("crolclient", "textures/arrows/arrow.png");

    @Inject(method = "getTexture", at = @At("HEAD"), cancellable = true)
    private void getTexture(CallbackInfoReturnable<Identifier> cir) {
        if (ConfigManager.getConfig().customArrowEnabled) {
            cir.setReturnValue(CUSTOM_ARROW);
        }
    }
}
