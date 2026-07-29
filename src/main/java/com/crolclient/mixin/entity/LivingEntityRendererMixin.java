package com.crolclient.mixin.entity;

import com.crolclient.config.ConfigManager;
import com.crolclient.render.esp.TargetESPRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ConfigManager.getConfig().targetEspEnabled && entity.equals(net.minecraft.client.MinecraftClient.getInstance().targetedEntity)) {
            // Target ESP render hook — can draw bounding box glow here
        }
    }
}
