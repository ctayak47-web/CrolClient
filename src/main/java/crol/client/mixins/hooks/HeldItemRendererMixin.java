package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.render.SwingAnimation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({HeldItemRenderer.class})
public class HeldItemRendererMixin {
   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      SwingAnimation swingAnimation = (SwingAnimation)CrolClient.INSTANCE.getModuleManager().getByClass(SwingAnimation.class);
      if (swingAnimation.isEnabled()) {
         if (hand == Hand.MAIN_HAND && MinecraftClient.getInstance().player.getMainHandStack().getItem() != Items.AIR && !(MinecraftClient.getInstance().player.getMainHandStack().getItem() instanceof FilledMapItem)) {
            swingAnimation.renderFirstPersonItemCustom(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
            ci.cancel();
         }

         if (hand == Hand.OFF_HAND && MinecraftClient.getInstance().player.getOffHandStack().getItem() != Items.AIR && !(MinecraftClient.getInstance().player.getOffHandStack().getItem() instanceof FilledMapItem)) {
            swingAnimation.renderFirstPersonItemCustom(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
            ci.cancel();
         }
      }

   }
}
