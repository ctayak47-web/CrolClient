package crol.client.managers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RotationManager {
   private float yaw;
   private float pitch;
   private boolean serverSprint = false;

   public void update(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setServerSprint(boolean serverSprint) {
      this.serverSprint = serverSprint;
   }

   public boolean isServerSprint() {
      return this.serverSprint;
   }
}
