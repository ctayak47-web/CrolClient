package crol.client.util.color;

import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.util.math.MathUtil;
import java.awt.Color;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ColorUtil {
   public static Color setAlpha(double anim, Color color) {
      double alpha = anim * (double)color.getAlpha() > (double)color.getAlpha() ? (double)color.getAlpha() : anim * (double)color.getAlpha();
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)alpha);
   }

   public static int pack(int red, int green, int blue, int alpha) {
      return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255) << 0;
   }

   public static int[] unpack(int color) {
      return new int[]{color >> 16 & 255, color >> 8 & 255, color & 255, color >> 24 & 255};
   }

   public static float[] normalize(Color color) {
      return new float[]{(float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F};
   }

   public static Color shiftHue(Color color, float degrees) {
      float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), (float[])null);
      hsb[0] = (hsb[0] + degrees / 360.0F) % 1.0F;
      if (hsb[0] < 0.0F) {
         int var10002 = (int) hsb[0] ++;
      }

      int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
      return new Color(rgb, true);
   }

   public static float[] normalize(int color) {
      int[] components = unpack(color);
      return new float[]{(float)components[0] / 255.0F, (float)components[1] / 255.0F, (float)components[2] / 255.0F, (float)components[3] / 255.0F};
   }

   public static float[] rgba(int color) {
      return new float[]{(float)(color >> 16 & 255) / 255.0F, (float)(color >> 8 & 255) / 255.0F, (float)(color & 255) / 255.0F, (float)(color >> 24 & 255) / 255.0F};
   }

   public static int gradient(int start, int end, int index, int speed) {
      if (speed == 0) {
         speed = (new Random()).nextInt(255);
      }

      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      angle = (angle > 180 ? 360 - angle : angle) + 180;
      int color = interpolate(start, end, MathHelper.clamp((float)angle / 180.0F - 1.0F, 0.0F, 1.0F));
      float[] hs = rgba(color);
      float[] hsb = Color.RGBtoHSB((int)(hs[0] * 255.0F), (int)(hs[1] * 255.0F), (int)(hs[2] * 255.0F), (float[])null);
      hsb[1] *= 1.5F;
      hsb[1] = Math.min(hsb[1], 1.0F);
      return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
   }

   public static int rgba(int r, int g, int b, int a) {
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static int interpolate(int start, int end, float value) {
      float[] startColor = rgba(start);
      float[] endColor = rgba(end);
      return rgba((int)MathUtil.interpolate((double)(startColor[0] * 255.0F), (double)(endColor[0] * 255.0F), (double)value), (int)MathUtil.interpolate((double)(startColor[1] * 255.0F), (double)(endColor[1] * 255.0F), (double)value), (int)MathUtil.interpolate((double)(startColor[2] * 255.0F), (double)(endColor[2] * 255.0F), (double)value), (int)MathUtil.interpolate((double)(startColor[3] * 255.0F), (double)(endColor[3] * 255.0F), (double)value));
   }

   public static int getGreen(int hex) {
      return hex >> 8 & 255;
   }

   public static int getRed(int hex) {
      return hex >> 16 & 255;
   }

   public static int getBlue(int hex) {
      return hex & 255;
   }

   public static int getAlpha(int hex) {
      return hex >> 24 & 255;
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return interpolate(oldValue, newValue, (float)interpolationValue);
   }

   public static int interpolateColor(int color1, int color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      int red1 = getRed(color1);
      int green1 = getGreen(color1);
      int blue1 = getBlue(color1);
      int alpha1 = getAlpha(color1);
      int red2 = getRed(color2);
      int green2 = getGreen(color2);
      int blue2 = getBlue(color2);
      int alpha2 = getAlpha(color2);
      int interpolatedRed = interpolateInt(red1, red2, (double)amount);
      int interpolatedGreen = interpolateInt(green1, green2, (double)amount);
      int interpolatedBlue = interpolateInt(blue1, blue2, (double)amount);
      int interpolatedAlpha = interpolateInt(alpha1, alpha2, (double)amount);
      return interpolatedAlpha << 24 | interpolatedRed << 16 | interpolatedGreen << 8 | interpolatedBlue;
   }

   public static Color colorAlpha(Color color, int alpha) {
      if (alpha > 255) {
         alpha = 255;
      }

      return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
   }

   public static void setColor(int color) {
      setAlphaColor(color, (float)(color >> 24 & 255) / 255.0F);
   }

   public static void setAlphaColor(int color, float alpha) {
      float red = (float)(color >> 16 & 255) / 255.0F;
      float green = (float)(color >> 8 & 255) / 255.0F;
      float blue = (float)(color & 255) / 255.0F;
      RenderSystem.setShaderColor(red, green, blue, alpha);
   }
}
