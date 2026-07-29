package com.crolclient.mixin;

import com.crolclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (ConfigManager.getConfig().fullbrightEnabled) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            client.options.getGamma().setValue(1000.0);
        }
    }
}
