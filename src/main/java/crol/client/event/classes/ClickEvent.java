package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClickEvent extends CancellableEvent {
   int action;
   int key;

   public ClickEvent(int key, int action) {
      this.key = key;
      this.action = action;
   }

   public int getAction() {
      return this.action;
   }

   public int getKey() {
      return this.key;
   }
}
