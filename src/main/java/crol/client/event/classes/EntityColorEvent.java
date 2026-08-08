package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EntityColorEvent extends CancellableEvent {
   private int color;

   public EntityColorEvent(int color) {
      this.color = color;
   }

   public void setColor(int color) {
      this.color = color;
   }

   public int getColor() {
      return this.color;
   }
}
