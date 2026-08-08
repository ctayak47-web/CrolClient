package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class InputEvent extends CancellableEvent {
   private float forward;
   private float strafe;
   private boolean jump;
   private boolean sneak;

   public InputEvent(float forward, float strafe, boolean jump, boolean sneak) {
      this.forward = forward;
      this.strafe = strafe;
      this.jump = jump;
      this.sneak = sneak;
   }

   public float getForward() {
      return this.forward;
   }

   public float getStrafe() {
      return this.strafe;
   }

   public boolean isJump() {
      return this.jump;
   }

   public boolean isSneak() {
      return this.sneak;
   }

   public void setForward(float forward) {
      this.forward = forward;
   }

   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   public void setJump(boolean jump) {
      this.jump = jump;
   }

   public void setSneak(boolean sneak) {
      this.sneak = sneak;
   }

   public void inputNone() {
      this.forward = 0.0F;
      this.strafe = 0.0F;
      this.jump = false;
      this.sneak = false;
   }
}
