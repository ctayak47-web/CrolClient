package crol.client.util.animations.impl;

import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EaseBackIn extends Animation {
   private final float easeAmount;

   public EaseBackIn(int ms, double endPoint, float easeAmount) {
      super(ms, endPoint);
      this.easeAmount = easeAmount;
   }

   public EaseBackIn(int ms, double endPoint, float easeAmount, Direction direction) {
      super(ms, endPoint, direction);
      this.easeAmount = easeAmount;
   }

   protected boolean correctOutput() {
      return true;
   }

   protected double getEquation(double x) {
      double x1 = x / (double)this.duration;
      float shrink = this.easeAmount + 1.0F;
      return Math.max((double)0.0F, (double)1.0F + (double)shrink * Math.pow(x1 - (double)1.0F, (double)3.0F) + (double)this.easeAmount * Math.pow(x1 - (double)1.0F, (double)2.0F));
   }
}
