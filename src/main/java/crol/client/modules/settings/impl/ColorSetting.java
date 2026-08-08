package crol.client.modules.settings.impl;

import crol.client.modules.settings.Setting;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ColorSetting extends Setting<ColorSetting> {
   private FloatSetting bright;
   private boolean isHovered = false;
   private FloatSetting alpha;
   private Color color;

   public ColorSetting color(Color color) {
      this.color = color;
      return this;
   }

   public boolean isHovered() {
      return this.isHovered;
   }

   public void setHovered(boolean hovered) {
      this.isHovered = hovered;
   }

   public Color getColor() {
      return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int)this.alpha.getValue());
   }

   public void setColor(Color color) {
      this.color = color;
   }

   public FloatSetting getAlpha() {
      return this.alpha;
   }

   public Color getDefColor() {
      return this.color;
   }

   public FloatSetting getBright() {
      return this.bright;
   }
}
