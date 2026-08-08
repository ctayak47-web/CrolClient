package crol.client.util.animations.impl;

import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DecelerateAnimation extends Animation {
   public DecelerateAnimation(int ms, double endPoint) {
      super(ms, endPoint);
   }

   public DecelerateAnimation(int ms, double endPoint, Direction direction) {
      super(ms, endPoint, direction);
   }

   protected double getEquation(double x) {
      double x1 = x / (double)this.duration;
      return (double)1.0F - (x1 - (double)1.0F) * (x1 - (double)1.0F);
   }
}
