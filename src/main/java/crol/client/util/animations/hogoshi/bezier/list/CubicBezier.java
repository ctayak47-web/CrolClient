package crol.client.util.animations.hogoshi.bezier.list;

import crol.client.util.animations.hogoshi.bezier.Bezier;
import crol.client.util.animations.hogoshi.bezier.Point;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CubicBezier extends Bezier {
   public double getValue(double t) {
      double dt = (double)1.0F - t;
      double dt2 = dt * dt;
      double t2 = t * t;
      Point temp = this.getPoint2().copy();
      return this.getStart().copy().scale(dt2, dt).add(temp.scale((double)3.0F * dt2 * t)).add(temp.set(this.getPoint3()).scale((double)3.0F * dt * t2)).add(temp.set(this.getEnd()).scale(t2 * t)).getY();
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private CubicBezier bezier = new CubicBezier();

      public Builder(CubicBezier bezier) {
         this.bezier = bezier;
      }

      public Builder() {
      }

      public Builder setPoint2(Point point) {
         this.bezier.setPoint2(point);
         return this;
      }

      public Builder setPoint3(Point point) {
         this.bezier.setPoint3(point);
         return this;
      }

      public CubicBezier build() {
         return this.bezier;
      }
   }
}
