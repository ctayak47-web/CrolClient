package crol.client.mixins.hooks;

import crol.client.CrolClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({TitleScreen.class})
public class TitleScreenMixin {
   @Inject(
      method = {"init"},
      at = {@At("RETURN")}
   )
   public void postInitHook(CallbackInfo ci) {
      MinecraftClient.getInstance().setScreen(CrolClient.INSTANCE.getMainScreen());
   }
}
