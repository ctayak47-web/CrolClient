package crol.client.util.math;

import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class TimerUtil implements IUtil {
   public long lastMS = System.currentTimeMillis();

   @Compile
   public void reset() {
      this.lastMS = System.currentTimeMillis();
   }

   public boolean isReached(long time) {
      return System.currentTimeMillis() - this.lastMS > time;
   }

   public void setLastMS(long newValue) {
      this.lastMS = System.currentTimeMillis() + newValue;
   }

   public void setTime(long time) {
      this.lastMS = time;
   }

   public boolean every(double delay) {
      boolean finished = this.isReached((long)delay);
      if (finished) {
         this.reset();
      }

      return finished;
   }

   public long getTime() {
      return System.currentTimeMillis() - this.lastMS;
   }

   public boolean isRunning() {
      return System.currentTimeMillis() - this.lastMS <= 0L;
   }

   public boolean hasTimeElapsed() {
      return this.lastMS < System.currentTimeMillis();
   }

   public long timeElapsed() {
      return this.lastMS - System.currentTimeMillis();
   }

   public long getLastMS() {
      return this.lastMS;
   }
}
