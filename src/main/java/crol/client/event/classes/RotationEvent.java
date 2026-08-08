package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RotationEvent extends CancellableEvent {
   private float yaw = 0.0F;
   private float pitch = 0.0F;

   public RotationEvent(float pitch, float yaw) {
      this.pitch = pitch;
      this.yaw = yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
