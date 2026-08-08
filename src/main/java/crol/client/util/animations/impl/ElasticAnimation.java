package crol.client.util.animations.impl;

import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ElasticAnimation extends Animation {
   float easeAmount;
   float smooth;
   boolean reallyElastic;

   public ElasticAnimation(int ms, double endPoint, float elasticity, float smooth, boolean moreElasticity) {
      super(ms, endPoint);
      this.easeAmount = elasticity;
      this.smooth = smooth;
      this.reallyElastic = moreElasticity;
   }

   public ElasticAnimation(int ms, double endPoint, float elasticity, float smooth, boolean moreElasticity, Direction direction) {
      super(ms, endPoint, direction);
      this.easeAmount = elasticity;
      this.smooth = smooth;
      this.reallyElastic = moreElasticity;
   }

   protected double getEquation(double x) {
      double x1 = Math.pow(x / (double)this.duration, (double)this.smooth);
      double elasticity = (double)(this.easeAmount * 0.1F);
      return Math.pow((double)2.0F, (double)-10.0F * (this.reallyElastic ? Math.sqrt(x1) : x1)) * (double)MathHelper.sin((float)((x1 - elasticity / (double)4.0F) * ((Math.PI * 2D) / elasticity))) + (double)1.0F;
   }
}
