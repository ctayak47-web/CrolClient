package crol.client.ui.notify;

import crol.client.util.animations.Animation;
import crol.client.util.math.TimerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Notify {
   String text;
   double x;
   double y;
   double width;
   Status status;
   Animation animation;
   TimerUtil timerUtil;

   public Notify(String text, double x, double y, double width, Status status, Animation animation, TimerUtil timerUtil) {
      this.text = text;
      this.x = x;
      this.y = y;
      this.width = width;
      this.status = status;
      this.animation = animation;
      this.timerUtil = timerUtil;
   }

   public String getText() {
      return this.text;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getWidth() {
      return this.width;
   }

   public Status getStatus() {
      return this.status;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public TimerUtil getTimerUtil() {
      return this.timerUtil;
   }

   public void setY(double y) {
      this.y = y;
   }
}
