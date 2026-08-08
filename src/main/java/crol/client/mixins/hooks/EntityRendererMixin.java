package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.render.PlayerEsp;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({EntityRenderer.class})
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
   @Inject(
      method = {"renderLabelIfPresent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderLabelIfPresent(S entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      PlayerEsp playerEsp = (PlayerEsp)CrolClient.INSTANCE.getModuleManager().getByClass(PlayerEsp.class);
      if (playerEsp.isEnabled() && playerEsp.options.getValueByName("Nametag") && this.isPlayer(entity.displayName)) {
         ci.cancel();
      }

   }

   boolean isPlayer(Text name) {
      if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         for(PlayerEntity player : MinecraftClient.getInstance().world.getPlayers()) {
            if (player.getDisplayName().equals(name)) {
               return true;
            }
         }
      }

      return false;
   }
}
