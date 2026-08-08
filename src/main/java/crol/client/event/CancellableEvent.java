package crol.client.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class CancellableEvent extends Event {
   private boolean cancelled;

   public void cancel() {
      this.cancelled = true;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void resetCancel() {
      this.cancelled = false;
   }
}
