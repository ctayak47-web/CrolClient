package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;

@Environment(EnvType.CLIENT)
public class HandledScreenEvent extends CancellableEvent {
   DrawContext drawContext;
   Slot slotHover;
   int backgroundWidth;
   int backgroundHeight;

   public HandledScreenEvent(DrawContext drawContext, Slot slotHover, int backgroundWidth, int backgroundHeight) {
      this.drawContext = drawContext;
      this.slotHover = slotHover;
      this.backgroundWidth = backgroundWidth;
      this.backgroundHeight = backgroundHeight;
   }

   public DrawContext getDrawContext() {
      return this.drawContext;
   }

   public Slot getSlotHover() {
      return this.slotHover;
   }

   public int getBackgroundWidth() {
      return this.backgroundWidth;
   }

   public int getBackgroundHeight() {
      return this.backgroundHeight;
   }
}
