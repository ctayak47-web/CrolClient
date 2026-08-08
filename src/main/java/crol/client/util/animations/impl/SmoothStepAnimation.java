package crol.client.util.animations.impl;

import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SmoothStepAnimation extends Animation {
   public SmoothStepAnimation(int ms, double endPoint) {
      super(ms, endPoint);
   }

   public SmoothStepAnimation(int ms, double endPoint, Direction direction) {
      super(ms, endPoint, direction);
   }

   protected double getEquation(double x) {
      double x1 = x / (double)this.duration;
      return (double)-2.0F * Math.pow(x1, (double)3.0F) + (double)3.0F * Math.pow(x1, (double)2.0F);
   }
}
