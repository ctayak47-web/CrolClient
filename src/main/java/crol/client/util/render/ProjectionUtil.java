package crol.client.util.render;

import crol.client.mixins.other.IMixinGameRenderer;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ProjectionUtil implements IUtil {
   public static Vector2f project(double x, double y, double z) {
      Camera camera = mc.getEntityRenderDispatcher().camera;
      Vec3d camPos = camera.getPos();
      double dx = x - camPos.x;
      double dy = y - camPos.y;
      double dz = z - camPos.z;
      double yawRad = Math.toRadians((double)(-camera.getYaw()));
      double pitchRad = Math.toRadians((double)(-camera.getPitch()));
      double cosYaw = Math.cos(yawRad);
      double sinYaw = Math.sin(yawRad);
      double xz = dx * cosYaw - dz * sinYaw;
      double zz = dx * sinYaw + dz * cosYaw;
      double cosPitch = Math.cos(pitchRad);
      double sinPitch = Math.sin(pitchRad);
      double yPrime = dy * cosPitch - zz * sinPitch;
      double zPrime = dy * sinPitch + zz * cosPitch;
      float scaledWidth = (float)mc.getWindow().getScaledWidth();
      float scaledHeight = (float)mc.getWindow().getScaledHeight();
      float fov = ((IMixinGameRenderer)mc.gameRenderer).getFov2(camera, mc.getRenderTickCounter().getTickDelta(false), true);
      float halfHeight = scaledHeight / 2.0F;
      if (zPrime > (double)0.0F) {
         double scale = (double)halfHeight / (zPrime * Math.tan(Math.toRadians((double)fov / (double)2.0F)));
         float screenX = (float)(-xz * scale + (double)scaledWidth / (double)2.0F);
         float screenY = (float)((double)scaledHeight / (double)2.0F - yPrime * scale);
         return new Vector2f(screenX, screenY);
      } else {
         return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
      }
   }
}
