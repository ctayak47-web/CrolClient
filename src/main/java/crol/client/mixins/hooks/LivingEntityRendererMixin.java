package crol.client.mixins.hooks;

import com.llamalad7.mixinextras.sugar.Local;
import crol.client.CrolClient;
import crol.client.event.classes.EntityColorEvent;
import crol.client.modules.impl.render.PlayerEsp;
import crol.client.util.color.ColorUtil;
import crol.client.util.render.esp.ChamsRenderer;
import crol.client.util.render.esp.FlatEspLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Shadow protected abstract @Nullable RenderLayer getRenderLayer(LivingEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline);

    /**
     * ИСПРАВЛЕНО: Добавлен LivingEntityRenderer<?, ?> renderer как первый аргумент.
     * Это обязательное требование для @Redirect методов экземпляра.
     */
    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;")
    )
    private RenderLayer renderHook(LivingEntityRenderer<?, ?, ?> renderer, LivingEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
        if (!translucent && state.width == 0.6F) {
            EntityColorEvent entityColorEvent = new EntityColorEvent(-1);
            CrolClient.INSTANCE.getEventManager().hookEvent(entityColorEvent);
            if (entityColorEvent.isCancelled()) {
                translucent = true;
            }
        }
        return this.getRenderLayer(state, showBody, translucent, showOutline);
    }

    /**
     * ИСПРАВЛЕНО: Аналогично, добавлен EntityModel<?> model как первый аргумент.
     * Также исправлено название метода для 1.21.4.
     */
    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")
    )
    private void redirectModelRender(EntityModel<?> model, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int color,
                                     @Local(argsOnly = true) LivingEntityRenderState renderState,
                                     @Local(argsOnly = true) VertexConsumerProvider vertexConsumers) {

        EntityColorEvent entityColorEvent = new EntityColorEvent(color);
        if (renderState.invisibleToPlayer) {
            CrolClient.INSTANCE.getEventManager().hookEvent(entityColorEvent);
        }

        PlayerEsp playerEsp = (PlayerEsp) CrolClient.INSTANCE.getModuleManager().getByClass(PlayerEsp.class);
        int themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB();

        boolean isPlayer = renderState instanceof PlayerEntityRenderState;
        boolean isEnabled = playerEsp != null && playerEsp.isEnabled() && playerEsp.options.getValueByName("Chams");

        if (isEnabled && isPlayer && playerEsp.chamsMode.is("Glass")) {
            // Пропускаем обычный рендер для Glass Chams
        } else {
            model.render(matrixStack, vertexConsumer, light, overlay, entityColorEvent.getColor());
        }

        if (isEnabled && isPlayer) {
            if (playerEsp.chamsMode.is("Glass")) {
                VertexConsumer depthVc = vertexConsumers.getBuffer(FlatEspLayer.FLAT_ESP_DEPTH);
                model.render(matrixStack, depthVc, light, overlay, -1);

                VertexConsumer colorVc = vertexConsumers.getBuffer(FlatEspLayer.FLAT_ESP);
                int glassColor = ColorUtil.setAlpha(0.4D, CrolClient.INSTANCE.getThemeManager().getTheme().color()).getRGB();
                model.render(matrixStack, colorVc, 15728880, overlay, glassColor);
            }

            if (playerEsp.chamsMode.is("Flat")) {
                VertexConsumer espVc = vertexConsumers.getBuffer(FlatEspLayer.FLAT_ESP);
                model.render(matrixStack, espVc, 15728880, overlay, themeColor);
            }

            if (playerEsp.chamsMode.is("New")) {
                int[] chams = this.adapt(CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB());
                ChamsRenderer.enqueue(model, matrixStack, chams);
            }
        }
    }

    @Unique
    private int[] adapt(int rgb) {
        int r = rgb >> 16 & 255;
        int g = rgb >> 8 & 255;
        int b = rgb & 255;
        float brightness = (r * 0.299F + g * 0.587F + b * 0.114F) / 255.0F;
        int overAlpha = Math.max(80, (int)(224.0F * (1.0F - brightness * 0.75F)));
        int throughAlpha = Math.max(37, (int)(112.0F * (1.0F - brightness * 0.65F)));
        return new int[]{overAlpha << 24 | r << 16 | g << 8 | b, throughAlpha << 24 | r << 16 | g << 8 | b};
    }
}