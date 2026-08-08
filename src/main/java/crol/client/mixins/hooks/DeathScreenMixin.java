package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.DeathScreenEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({DeathScreen.class})
public class DeathScreenMixin {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      CrolClient.INSTANCE.getEventManager().hookEvent(new DeathScreenEvent());
   }
}
