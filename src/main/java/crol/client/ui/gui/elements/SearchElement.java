package crol.client.ui.gui.elements;

import crol.client.managers.FontManager;
import crol.client.ui.gui.IElementable;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MouseUtil;
import crol.client.util.other.UtfUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class SearchElement implements IElementable {
   private boolean select = false;
   private String value = "";
   private double animation;
   private int key = 0;
   private String type;

   public void mouseClick(int click) {
      switch (click) {
         case 0:
            this.select = true;
         default:
      }
   }

   public void render(double x, double y, double width, double height, double mouseX, double mouseY, DrawContext drawContext) {
      Color color = ColorUtil.setAlpha(this.animation, Color.WHITE);
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      String endValue = this.value.equalsIgnoreCase("") && !this.select ? "Search" : (!this.select ? this.value : this.value + "_");
      if (!endValue.isEmpty()) {
         BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(endValue).color(color).size(8.0F).thickness(0.05F).build();
         text.render(matrix, x + (double)8.0F, y + (double)10.0F);
      }

   }

   public void button(double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      this.render(x, y, width, height, mouseX, mouseY, drawContext);
      if (MouseUtil.isHovered((double)((int)x), (double)((int)y), (double)((int)width), (double)((int)height), (double)((int)mouseX), (double)((int)mouseY))) {
         this.mouseClick(click);
      }

      if (this.select) {
         if (!this.type.isEmpty() && this.value.toCharArray().length < 23 && !UtfUtil.containsRussianLetter(this.type)) {
            this.value = this.value + this.type;
         }

         if (this.key == 259) {
            String textEnd = "";
            char[] text2 = this.value.toCharArray();

            for(int i = 0; i < text2.length; ++i) {
               if (i < text2.length - 1) {
                  textEnd = textEnd + text2[i];
               }
            }

            this.value = textEnd;
         }

         if (this.key == 32) {
            this.value = this.value + " ";
         }

         if (this.key == 257) {
            this.select = false;
         }
      }

   }

   public void setAnimation(double animation) {
      this.animation = animation;
   }

   public void setType(String type) {
      this.type = type;
   }

   public void setKey(int key) {
      this.key = key;
   }

   public String getValue() {
      return this.value;
   }

   public void setSelect(boolean select) {
      this.select = select;
   }
}
