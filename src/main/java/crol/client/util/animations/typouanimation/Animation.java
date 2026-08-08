package crol.client.util.animations.typouanimation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Animation {
   private Easing easing;
   private long duration;
   private long millis;
   private long startTime;
   private double startValue;
   private double destinationValue;
   private double value;
   private boolean finished;

   public Animation(Easing easing, long duration) {
      this.easing = easing;
      this.startTime = System.currentTimeMillis();
      this.duration = duration;
   }

   public void run(double destinationValue) {
      this.millis = System.currentTimeMillis();
      if (this.destinationValue != destinationValue) {
         this.destinationValue = destinationValue;
         this.reset();
      } else {
         this.finished = this.millis - this.duration > this.startTime;
         if (this.finished) {
            this.value = destinationValue;
            return;
         }
      }

      double result = (Double)this.easing.getFunction().apply(this.getProgress());
      if (this.value > destinationValue) {
         this.value = this.startValue - (this.startValue - destinationValue) * result;
      } else {
         this.value = this.startValue + (destinationValue - this.startValue) * result;
      }

   }

   public double getProgress() {
      return (double)(System.currentTimeMillis() - this.startTime) / (double)this.duration;
   }

   public void reset() {
      this.startTime = System.currentTimeMillis();
      this.startValue = this.value;
      this.finished = false;
   }
}
