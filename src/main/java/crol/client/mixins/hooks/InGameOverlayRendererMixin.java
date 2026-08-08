package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.util.NoRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin {
   @Inject(
      method = {"renderFireOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      NoRender noRender = (NoRender)CrolClient.INSTANCE.getModuleManager().getByClass(NoRender.class);
      if (noRender.isEnabled() && noRender.options.getValueByName("Fire")) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderInWallOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderInWallOverlay(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      NoRender noRender = (NoRender)CrolClient.INSTANCE.getModuleManager().getByClass(NoRender.class);
      if (noRender.isEnabled() && noRender.options.getValueByName("Wall")) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderUnderwaterOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      NoRender noRender = (NoRender)CrolClient.INSTANCE.getModuleManager().getByClass(NoRender.class);
      if (noRender.isEnabled() && noRender.options.getValueByName("Water")) {
         ci.cancel();
      }

   }
}
