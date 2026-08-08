package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.player.FreeCam;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({AbstractClientPlayerEntity.class})
public class AbstractClientPlayerEntityMixin {
   @Inject(
      method = {"isSpectator"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void isSpectator(CallbackInfoReturnable<Boolean> cir) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null) {
         if (CrolClient.INSTANCE.getModuleManager().getByClass(FreeCam.class).isEnabled()) {
            cir.setReturnValue(true);
         }

      }
   }
}
