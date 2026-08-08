package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MoveCorrectionEvent extends CancellableEvent {
   private float yaw;
   private float pitch;

   public MoveCorrectionEvent(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
