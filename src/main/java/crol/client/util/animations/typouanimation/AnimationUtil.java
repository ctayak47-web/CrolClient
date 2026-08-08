package crol.client.util.animations.typouanimation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AnimationUtil {
   long mc;
   float anim;
   public float to;
   public float speed;

   public AnimationUtil(float anim, float to, float speed) {
      this.anim = anim;
      this.to = to;
      this.speed = speed;
      this.mc = System.currentTimeMillis();
   }

   public static AnimationUtil create(float anim, float to, float speed) {
      return new AnimationUtil(anim, to, speed);
   }

   public float getAnim() {
      int count = (int)((System.currentTimeMillis() - this.mc) / 5L);
      if (count > 0) {
         this.mc = System.currentTimeMillis();
      }

      for(int i = 0; i < count; ++i) {
         this.anim += (this.to - this.anim) * this.speed;
      }

      return this.anim;
   }

   public void reset() {
      this.mc = System.currentTimeMillis();
   }

   public void setAnim(float anim) {
      this.anim = anim;
      this.mc = System.currentTimeMillis();
   }

   public void setTo(float to) {
      this.to = to;
   }

   public float getTo() {
      return this.to;
   }

   public void setSpeed(float speed) {
      this.speed = speed;
   }
}
