package crol.client.ui.gui.elements;

import crol.client.managers.FontManager;
import crol.client.modules.settings.impl.KeySetting;
import crol.client.ui.gui.IElementable;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MouseUtil;
import crol.client.util.other.BindUtill;
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
public class KeyElement implements IElementable {
   private KeySetting keySetting;
   private int key;
   private double animation;
   private double animationEnabled;

   public void mouseClick(int click) {
      switch (click) {
         case 0:
            this.keySetting.setSelect(true);
         default:
      }
   }

   public void render(double x, double y, double width, double height, double mouseX, double mouseY, DrawContext drawContext) {
      y += (double)2.5F;
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      String text = this.keySetting.isSelect() ? "press" : BindUtill.getBind(this.keySetting.getValue());
      boolean animationFinished = this.animationEnabled > (double)0.0F;
      Color colorText = ColorUtil.setAlpha(this.animation, ColorUtil.colorAlpha(new Color(67108863, true), (int)(122.4 + (animationFinished ? 132.6 * this.animationEnabled : (double)0.0F))));
      BuiltRectangle rect = (BuiltRectangle)Builder.rectangle().size(new SizeState(((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(text, 6.0F) + 6.0F, 10.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(67108863, true)))).radius(new QuadRadiusState(2.0F)).smoothness(1.2F).build();
      rect.render(matrix, x + width - (double)((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(text, 6.0F) - (double)6.0F, y - (double)0.5F);
      BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(text, 6.0F) + 6.0F, 10.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(184549375, true)))).radius(new QuadRadiusState(2.0F)).thickness(0.22F).smoothness(0.5F, 0.5F).build();
      border.render(matrix, x + width - (double)((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(text, 6.0F) - (double)6.0F, y - (double)0.5F);
      BuiltText textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.keySetting.getName()).color(colorText).size(7.5F).thickness(0.05F).build();
      textRender.render(matrix, x, y + (double)1.0F);
      textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(text).color(ColorUtil.setAlpha(this.animation, Color.gray)).size(6.0F).thickness(0.05F).build();
      textRender.render(matrix, x + width - (double)((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(text, 6.0F) - (double)3.5F, y + (double)1.0F);
   }

   public void button(double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      this.render(x, y, width, height, mouseX, mouseY, drawContext);
      if (this.keySetting.isSelect() && this.key != 0) {
         int endKey = this.key != 261 && this.key != 259 && this.key != 256 ? this.key : -1;
         this.keySetting.setValue(endKey);
         this.keySetting.setSelect(false);
      }

      if (MouseUtil.isHovered((double)((int)x), (double)((int)y), (double)((int)width), (double)((int)height), (double)((int)mouseX), (double)((int)mouseY))) {
         this.mouseClick(click);
      }

   }

   public void setKeySetting(KeySetting keySetting) {
      this.keySetting = keySetting;
   }

   public void setKey(int key) {
      this.key = key;
   }

   public void setAnimation(double animation) {
      this.animation = animation;
   }

   public void setAnimationEnabled(double animationEnabled) {
      this.animationEnabled = animationEnabled;
   }
}
