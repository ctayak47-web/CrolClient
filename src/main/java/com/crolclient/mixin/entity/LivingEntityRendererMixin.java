package com.crolclient.mixin.entity;

import com.crolclient.config.ConfigManager;
import com.crolclient.render.esp.TargetESPRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void onRender(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // Проверяем включен ли ESP
        if (ConfigManager.getConfig().targetEspEnabled) {
            var client = net.minecraft.client.MinecraftClient.getInstance();
            // В 1.21.4 можно сверить текущую сущность через клиент или контекст рендеринга
            if (client.targetedEntity instanceof LivingEntity targetEntity) {
                // Вызываем логику отрисовки Target ESP
                // TargetESPRenderer.render(matrices, vertexConsumers, targetEntity);
            }
        }
    }
}

