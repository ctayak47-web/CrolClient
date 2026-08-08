package crol.client.ui.altmanager.impl;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.ui.altmanager.Type;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MouseUtil;
import crol.client.util.other.UtfUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBorder;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class StringElement {
   private boolean select = false;
   private String value = "";
   private int key = 0;
   private String type;
   private final Type typeElement;
   private final Animation animation;

   public StringElement(Type typeElement) {
      this.typeElement = typeElement;
      this.animation = new EaseBackIn(400, (double)1.0F, 0.1F, Direction.BACKWARDS);
   }

   public void mouseClick(int click) {
      switch (click) {
         case 0:
            switch (this.typeElement) {
               case NAME -> CrolClient.INSTANCE.getAltManagerScreen().getTagElement().resetSelect();
               case TAG -> CrolClient.INSTANCE.getAltManagerScreen().getNameElement().resetSelect();
            }

            this.select = true;
            this.animation.setDirection(Direction.FORWARDS);
         default:
      }
   }

   public void render(double x, double y, double width, double height, double mouseX, double mouseY, DrawContext drawContext) {
      Color color = new Color(12961746);
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      String defaultText = this.typeElement == Type.NAME ? "Name" : "Tag";
      String endValue = this.value.equalsIgnoreCase("") && !this.select ? defaultText : this.value;
      ((BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(new Color(-14342350, true))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build()).render(matrix, x, y);
      ((BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(new Color(3092796))).radius(new QuadRadiusState(5.0F)).thickness(0.01F).smoothness(0.6F, 0.6F).build()).render(matrix, x, y);
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.MAINMENU.get()).text(this.typeElement == Type.NAME ? "L" : "X").color(new Color(-11710630, true)).size(8.0F).thickness(0.05F).build()).render(matrix, x + (double)7.0F, y + 5.9);
      if (!this.animation.finished(Direction.BACKWARDS)) {
         ((BuiltText)Builder.text().font((MsdfFont)FontManager.MAINMENU.get()).text(this.typeElement == Type.NAME ? "L" : "X").color(ColorUtil.setAlpha(this.animation.getOutput(), new Color(4241151))).size(8.0F).thickness(0.05F).build()).render(matrix, x + (double)7.0F, y + 5.9);
      }

      if (!endValue.isEmpty()) {
         BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(endValue).color(color).size(8.0F).thickness(0.05F).build();
         text.render(matrix, x + (double)20.0F, y + (double)5.0F);
         if (!this.animation.finished(Direction.BACKWARDS)) {
            text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(endValue).color(ColorUtil.setAlpha(this.animation.getOutput(), Color.white)).size(8.0F).thickness(0.05F).build();
            text.render(matrix, x + (double)20.0F, y + (double)5.0F);
         }
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
            this.animation.setDirection(Direction.BACKWARDS);
         }
      }

   }

   public void setType(String type) {
      this.type = type;
   }

   public void setKey(int key) {
      this.key = key;
   }

   public void setSelect(boolean select) {
      this.select = select;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public void resetSelect() {
      this.setSelect(false);
      this.animation.setDirection(Direction.BACKWARDS);
   }
}
