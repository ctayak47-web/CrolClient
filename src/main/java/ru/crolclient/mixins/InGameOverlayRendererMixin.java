package ru.crolclient.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.events.render.DrawOverlayObjectEvent;
import ru.crolclient.implement.features.modules.render.ClearRenderModule;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
    private static void renderOverlays(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        DrawOverlayObjectEvent event = new DrawOverlayObjectEvent(DrawOverlayObjectEvent.OverlayType.FIRE_OVERLAY, false);
        Extra.getInstance().getEventManager().callEvent(event);

        ClearRenderModule clearRenderModule = (ClearRenderModule) Extra.getInstance().getModuleProvider().module("ClearRender");
        if (clearRenderModule != null && clearRenderModule.isState()) {
            if (clearRenderModule.getClearRenderSettings().isSelected("Fire") && event.getOverlayType() == DrawOverlayObjectEvent.OverlayType.FIRE_OVERLAY) {
                event.cancel();
            }
        }

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        DrawOverlayObjectEvent event = new DrawOverlayObjectEvent(DrawOverlayObjectEvent.OverlayType.WATER_OVERLAY, false);
        Extra.getInstance().getEventManager().callEvent(event);

        ClearRenderModule clearRenderModule = (ClearRenderModule) Extra.getInstance().getModuleProvider().module("ClearRender");
        if (clearRenderModule != null && clearRenderModule.isState()) {
            if (clearRenderModule.getClearRenderSettings().isSelected("Water") && event.getOverlayType() == DrawOverlayObjectEvent.OverlayType.WATER_OVERLAY) {
                event.cancel();
            }
        }

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
