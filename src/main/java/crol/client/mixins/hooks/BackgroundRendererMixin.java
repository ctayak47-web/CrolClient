package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.render.BetterWorld;
import crol.client.util.color.ColorUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({BackgroundRenderer.class})
public class BackgroundRendererMixin {
   private static BetterWorld cachedModule = null;
   private static boolean moduleLookupDone = false;
   private static BetterWorld BETTER_WORLD;

   @Inject(
      method = {"applyFog"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
      BetterWorld betterWorld = getModule();
      if (betterWorld.isEnabled() && betterWorld.fog.getValue()) {
         float[] rgba = ColorUtil.rgba(CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB());
         float start = betterWorld.start.getValue();
         float end = betterWorld.end.getValue();
         Fog customFog = new Fog(start, end, FogShape.CYLINDER, rgba[0], rgba[1], rgba[2], betterWorld.alphaFog.getValue() / 100.0F);
         cir.setReturnValue(customFog);
      }

   }

   @Inject(
      method = {"getFogColor"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private static void onGetFogColor(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
      BetterWorld betterWorld = getModule();
      if (betterWorld.isEnabled() && betterWorld.fog.getValue()) {
         float[] rgba = ColorUtil.rgba(CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB());
         cir.setReturnValue(new Vector4f(rgba[0], rgba[1], rgba[2], betterWorld.alphaFog.getValue() / 100.0F));
      }

   }

   private static BetterWorld getModule() {
      if (!moduleLookupDone) {
         moduleLookupDone = true;

         try {
            cachedModule = (BetterWorld)CrolClient.INSTANCE.getModuleManager().getByClass(BetterWorld.class);
         } catch (Exception var1) {
            cachedModule = null;
         }
      }

      return cachedModule;
   }
}
