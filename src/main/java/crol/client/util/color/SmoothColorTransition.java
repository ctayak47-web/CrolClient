package crol.client.util.color;

import crol.client.util.math.TimerUtil;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SmoothColorTransition {
   private final TimerUtil timer = new TimerUtil();
   private Color startColor;
   private Color targetColor;
   private boolean transitioning = false;
   private float duration = 350.0F;
   private int currentRed;
   private int currentGreen;
   private int currentBlue;
   private int currentAlpha;

   public SmoothColorTransition(Color color) {
      this.setColor(color);
   }

   public void transitionTo(Color target) {
      if (this.transitioning) {
         this.startColor = this.getCurrentColor();
      } else {
         this.startColor = this.getCurrentColor();
      }

      this.targetColor = target;
      this.timer.reset();
      this.transitioning = true;
   }

   public Color getCurrentColor() {
      if (!this.transitioning) {
         return new Color(this.currentRed, this.currentGreen, this.currentBlue, this.currentAlpha);
      } else {
         float progress = Math.min((float)this.timer.getTime() / this.duration, 1.0F);
         if (progress >= 1.0F) {
            this.currentRed = this.targetColor.getRed();
            this.currentGreen = this.targetColor.getGreen();
            this.currentBlue = this.targetColor.getBlue();
            this.currentAlpha = this.targetColor.getAlpha();
            this.transitioning = false;
         } else {
            this.currentRed = this.interpolate(this.startColor.getRed(), this.targetColor.getRed(), progress);
            this.currentGreen = this.interpolate(this.startColor.getGreen(), this.targetColor.getGreen(), progress);
            this.currentBlue = this.interpolate(this.startColor.getBlue(), this.targetColor.getBlue(), progress);
            this.currentAlpha = this.interpolate(this.startColor.getAlpha(), this.targetColor.getAlpha(), progress);
         }

         return new Color(this.currentRed, this.currentGreen, this.currentBlue, this.currentAlpha);
      }
   }

   public int getCurrentRGB() {
      Color color = this.getCurrentColor();
      return color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

   public int getCurrentARGB() {
      Color color = this.getCurrentColor();
      return color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

   private int interpolate(int start, int end, float progress) {
      return Math.round((float)start + (float)(end - start) * progress);
   }

   public void setColor(Color color) {
      this.startColor = color;
      this.targetColor = color;
      this.currentRed = color.getRed();
      this.currentGreen = color.getGreen();
      this.currentBlue = color.getBlue();
      this.currentAlpha = color.getAlpha();
      this.transitioning = false;
   }

   public boolean isTransitioning() {
      return this.transitioning;
   }

   public void setDuration(float milliseconds) {
      this.duration = milliseconds;
   }

   public float getProgress() {
      return !this.transitioning ? 1.0F : Math.min((float)this.timer.getTime() / this.duration, 1.0F);
   }
}
