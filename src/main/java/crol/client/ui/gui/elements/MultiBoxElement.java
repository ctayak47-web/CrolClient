package crol.client.ui.gui.elements;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.ui.gui.IElementable;
import crol.client.util.animations.Direction;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MouseUtil;
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
public class MultiBoxElement implements IElementable {
   private MultiBoxSetting multiBoxSetting;
   private double animation;
   private double animationEnabled;
   private float offsetX = 0.0F;
   private float offsetY = 0.0F;

   public void mouseClick(int click) {
   }

   public void render(double x, double y, double width, double height, double mouseX, double mouseY, DrawContext drawContext) {
   }

   public void button(double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      boolean animationFinished = this.animationEnabled > (double)0.0F;
      Color colorText = ColorUtil.setAlpha(this.animation, ColorUtil.colorAlpha(new Color(67108863, true), (int)(122.4 + (animationFinished ? 132.6 * this.animationEnabled : (double)0.0F))));
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.MONTSERRAT.get()).text(this.multiBoxSetting.getName()).color(colorText).size(7.5F).thickness(0.05F).build();
      text.render(matrix, x, y + (double)3.5F);
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, (double)(this.multiBoxSetting.getOffset() + 16.0F))).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(67108863, true)))).radius(new QuadRadiusState(3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x, y + (double)16.0F);
      BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(width, (double)(this.multiBoxSetting.getOffset() + 16.0F))).color(new QuadColorState(ColorUtil.colorAlpha(new Color(67108863, true), (int)((double)3.0F * this.animation)))).radius(new QuadRadiusState(3.0F)).thickness(0.01F).smoothness(0.65F, 0.65F).build();
      border.render(matrix, x, y + (double)16.0F);
      this.offsetX = 0.0F;
      this.offsetY = 0.0F;

      for(BooleanSetting booleanSetting : this.multiBoxSetting.getBooleanSettings()) {
         if ((double)(this.offsetX + ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(booleanSetting.getName(), 7.0F) + 5.0F) > width - (double)8.0F) {
            this.offsetY += 14.0F;
            this.offsetX = 0.0F;
         }

         this.rendereMultiboxButtonButton(booleanSetting, x + (double)2.0F + (double)this.offsetX, y + (double)18.0F + (double)this.offsetY, (double)(((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(booleanSetting.getName().toUpperCase(), 7.0F) + 5.0F), (double)12.0F, mouseX, mouseY, xStart, yStart, xEnd, yEnd, click, matrix);
         this.offsetX += ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(booleanSetting.getName().toUpperCase(), 7.0F) + 8.0F;
      }

      this.multiBoxSetting.setOffset(this.offsetY);
   }

   private void rendereMultiboxButtonButton(BooleanSetting booleanSetting, double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, int click, Matrix4f matrix) {
      boolean animationFinished = this.animationEnabled > (double)0.0F;
      Color colorText = ColorUtil.setAlpha(this.animation, ColorUtil.colorAlpha(new Color(67108863, true), (int)(122.4 + (animationFinished ? 132.6 * this.animationEnabled : (double)0.0F))));
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(booleanSetting.getName().toUpperCase()).color(ColorUtil.setAlpha((animationFinished && !booleanSetting.getAnimation().finished(Direction.BACKWARDS) ? (double)1.0F - booleanSetting.getAnimation().getOutput() : (double)1.0F) * this.animation, colorText)).size(7.0F).thickness(0.05F).build();
      text.render(matrix, x + (double)2.0F, y + (double)2.0F);
      if (!booleanSetting.getAnimation().finished(Direction.BACKWARDS)) {
         BuiltRectangle rect = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation * booleanSetting.getAnimation().getOutput(), new Color(150994943, true)))).radius(new QuadRadiusState(2.0F)).smoothness(1.2F).build();
         rect.render(matrix, x, y);
         BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.colorAlpha(new Color(67108863, true), (int)((double)7.0F * this.animation * booleanSetting.getAnimation().getOutput())))).radius(new QuadRadiusState(2.0F)).thickness(0.01F).smoothness(0.65F, 0.65F).build();
         border.render(matrix, x, y);
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(booleanSetting.getName().toUpperCase()).color(ColorUtil.setAlpha((animationFinished && !booleanSetting.getAnimation().finished(Direction.BACKWARDS) ? (double)1.0F - booleanSetting.getAnimation().getOutput() : (double)1.0F) * this.animation * booleanSetting.getAnimation().getOutput(), new Color(150994943, true))).size(7.0F).thickness(0.05F).build();
         text.render(matrix, x + (double)2.0F, y + (double)2.0F);
         if (animationFinished) {
            text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(booleanSetting.getName().toUpperCase()).color(ColorUtil.setAlpha(this.animation * booleanSetting.getAnimation().getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color())).size(7.0F).thickness(0.05F).build();
            text.render(matrix, x + (double)2.0F, y + (double)2.0F);
         }
      }

      if (MouseUtil.isHovered((double)((int)x), (double)((int)y), (double)((int)width), (double)((int)height), (double)((int)mouseX), (double)((int)mouseY)) && click == 0 && MouseUtil.isHovered2(xStart, yStart, xEnd, yEnd, mouseX, mouseY)) {
         booleanSetting.setValue(!booleanSetting.getValue());
      }

   }

   public void setAnimationEnabled(double animationEnabled) {
      this.animationEnabled = animationEnabled;
   }

   public void setAnimation(double animation) {
      this.animation = animation;
   }

   public void setMultiBoxSetting(MultiBoxSetting multiBoxSetting) {
      this.multiBoxSetting = multiBoxSetting;
   }
}
