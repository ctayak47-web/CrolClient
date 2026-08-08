package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.util.NoRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({LightmapTextureManager.class})
public class LightmapTextureManagerMixin {
   @Inject(
      method = {"getDarknessFactor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getDarknessFactor(float delta, CallbackInfoReturnable<Float> cir) {
      NoRender noRender = (NoRender)CrolClient.INSTANCE.getModuleManager().getByClass(NoRender.class);
      if (noRender.isEnabled() && noRender.options.getValueByName("Darkness")) {
         cir.setReturnValue(0.0F);
      }

   }
}
