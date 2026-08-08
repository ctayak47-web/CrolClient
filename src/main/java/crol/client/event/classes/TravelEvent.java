package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TravelEvent extends CancellableEvent {
   private double yaw;
   private double pitch;

   public TravelEvent(double yaw, double pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public double getYaw() {
      return this.yaw;
   }

   public double getPitch() {
      return this.pitch;
   }

   public void setYaw(double yaw) {
      this.yaw = yaw;
   }

   public void setPitch(double pitch) {
      this.pitch = pitch;
   }
}
