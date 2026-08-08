package crol.client.util.render;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SkyConfig {
   public static Color BASE_COLOR = new Color(13, 26, 255);
   public static float[] SKY_ZENITH;
   public static float[] SKY_HORIZON;
   public static float[] NEB_COLOR1;
   public static float[] NEB_COLOR2;
   public static float NEB_INTENSITY = 0.75F;
   public static float[] STAR_COLOR;
   private static int lastComputedRgb = Integer.MIN_VALUE;

   public static void compute() {
      int rgb = BASE_COLOR.getRGB();
      if (rgb != lastComputedRgb) {
         lastComputedRgb = rgb;
         float[] hsb = Color.RGBtoHSB(BASE_COLOR.getRed(), BASE_COLOR.getGreen(), BASE_COLOR.getBlue(), (float[])null);
         float h = hsb[0];
         float s = hsb[1];
         float b = hsb[2];
         SKY_ZENITH = hsb2rgb(h, clamp(s * 0.9F), clamp(b * 0.12F));
         SKY_HORIZON = hsb2rgb(h, clamp(s * 0.7F), clamp(b * 0.28F));
         NEB_COLOR1 = hsb2rgb(hueShift(h, 20.0F), clamp(s * 1.1F), clamp(b * 0.65F));
         NEB_COLOR2 = hsb2rgb(hueShift(h, -15.0F), clamp(s * 0.85F), clamp(b * 0.4F));
         STAR_COLOR = hsb2rgb(hueShift(h, 40.0F), clamp(s * 0.9F), clamp(b * 0.85F));
         SkyShaderHolder.markDirty();
      }
   }

   public static void setBaseColor(Color color) {
      BASE_COLOR = color;
      compute();
   }

   private static float hueShift(float h, float angleDeg) {
      float shifted = h + angleDeg / 360.0F;
      return shifted - (float)Math.floor((double)shifted);
   }

   private static float clamp(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   private static float[] hsb2rgb(float h, float s, float b) {
      int packed = Color.HSBtoRGB(h, s, b);
      return new float[]{(float)(packed >> 16 & 255) * 0.003921569F, (float)(packed >> 8 & 255) * 0.003921569F, (float)(packed & 255) * 0.003921569F};
   }

   private SkyConfig() {
   }

   static {
      compute();
   }
}
