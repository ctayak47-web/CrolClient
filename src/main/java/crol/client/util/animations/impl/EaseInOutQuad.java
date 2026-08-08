package crol.client.util.animations.impl;

import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EaseInOutQuad extends Animation {
   public EaseInOutQuad(int ms, double endPoint) {
      super(ms, endPoint);
   }

   public EaseInOutQuad(int ms, double endPoint, Direction direction) {
      super(ms, endPoint, direction);
   }

   protected double getEquation(double x1) {
      double x = x1 / (double)this.duration;
      return x < (double)0.5F ? (double)2.0F * Math.pow(x, (double)2.0F) : (double)1.0F - Math.pow((double)-2.0F * x + (double)2.0F, (double)2.0F) / (double)2.0F;
   }
}
