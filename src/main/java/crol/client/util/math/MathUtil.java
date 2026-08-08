package crol.client.util.math;

import crol.client.util.IUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

@Environment(EnvType.CLIENT)
public class MathUtil implements IUtil {
   public static float fast(float end, float start, float multiple) {
      return (1.0F - MathHelper.clamp((float)(deltaTime() * (double)multiple), 0.0F, 1.0F)) * end + MathHelper.clamp((float)(deltaTime() * (double)multiple), 0.0F, 1.0F) * start;
   }

   public static Vector3d fast(Vector3d end, Vector3d start, float multiple) {
      return new Vector3d((double)fast((float)end.x, (float)start.x, multiple), (double)fast((float)end.y, (float)start.y, multiple), (double)fast((float)end.z, (float)start.z, multiple));
   }

   public static Vec3d fast(Vec3d end, Vec3d start, float multiple) {
      return new Vec3d((double)fast((float)end.x, (float)start.x, multiple), (double)fast((float)end.y, (float)start.y, multiple), (double)fast((float)end.z, (float)start.z, multiple));
   }

   public static double deltaTime() {
      return MinecraftClient.getInstance().getCurrentFps() > 0 ? (double)1.0F / (double)MinecraftClient.getInstance().getCurrentFps() : (double)1.0F;
   }

   public static float getTickDelta() {
      return mc.getRenderTickCounter().getTickDelta(false);
   }

   public static double round(double num, double increment) {
      double v = (double)Math.round(num / increment) * increment;
      BigDecimal bd = new BigDecimal(v);
      bd = bd.setScale(2, RoundingMode.HALF_UP);
      return bd.doubleValue();
   }

   public static double interpolate(double current, double old, double scale) {
      return old + (current - old) * scale;
   }

   public static float random(float min, float max) {
      return min + ThreadLocalRandom.current().nextFloat() * (max - min);
   }

   public static int getRandom(int min, int max) {
      return (int)getRandom((float)min, (float)max + 1.0F);
   }

   public static float getRandom(float min, float max) {
      return (float)getRandom((double)min, (double)max);
   }

   public static Vec3d interpolate(Vec3d prevPos, Vec3d pos) {
      return new Vec3d(interpolate(prevPos.x, pos.x), interpolate(prevPos.y, pos.y), interpolate(prevPos.z, pos.z));
   }

   public static double interpolate(double prev, double orig) {
      return org.joml.Math.lerp((double)tickCounter.getTickDelta(false), prev, orig);
   }

   public static double getRandom(double min, double max) {
      if (min == max) {
         return min;
      } else {
         if (min > max) {
            double d = min;
            min = max;
            max = d;
         }

         return ThreadLocalRandom.current().nextDouble(min, max);
      }
   }
}
