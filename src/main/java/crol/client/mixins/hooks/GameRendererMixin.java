package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.player.NoEntityTrace;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"findCrosshairTarget"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void findCrosshairTargetHook(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
      NoEntityTrace noEntityTrace = (NoEntityTrace)CrolClient.INSTANCE.getModuleManager().getByClass(NoEntityTrace.class);
      if (noEntityTrace != null && noEntityTrace.shouldIgnoreEntityTrace()) {
         double d = Math.max(blockInteractionRange, entityInteractionRange);
         HitResult hitResult = camera.raycast(d, tickDelta, false);
         cir.setReturnValue(hitResult);
      }

   }
}
