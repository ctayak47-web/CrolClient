package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

@Environment(EnvType.CLIENT)
public class Render2DEvent extends CancellableEvent {
   private DrawContext drawContext;
   private RenderTickCounter tickCounter;

   public void update(DrawContext drawContext, RenderTickCounter tickCounter) {
      this.drawContext = drawContext;
      this.tickCounter = tickCounter;
   }

   public DrawContext getDrawContext() {
      return this.drawContext;
   }

   public RenderTickCounter getTickCounter() {
      return this.tickCounter;
   }
}
