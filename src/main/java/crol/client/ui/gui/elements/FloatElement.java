package crol.client.ui.gui.elements;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.ui.gui.IElementable;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.math.MouseUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class FloatElement implements IElementable {
   private FloatSetting floatSetting;
   private double animation;
   private double animationEnabled;

   public void mouseClick(int click) {
      if (click == 0) {
         this.floatSetting.setSlide(true);
      }

   }

   public void render(double x, double y, double width, double height, double mouseX, double mouseY, DrawContext drawContext) {
      float amountWidth = (MathHelper.clamp(this.floatSetting.getValue(), this.floatSetting.getMinValue(), this.floatSetting.getMaxValue()) - this.floatSetting.getMinValue()) / (this.floatSetting.getMaxValue() - this.floatSetting.getMinValue());
      this.floatSetting.setAnimationValue(MathUtil.fast(this.floatSetting.getAnimationValue(), amountWidth, 4.0F));
      this.floatSetting.setAnimationValue(MathHelper.clamp(this.floatSetting.getAnimationValue(), 0.0F, 1.0F));
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      boolean animationFinished = this.animationEnabled > (double)0.0F;
      Color colorText = ColorUtil.setAlpha(this.animation, ColorUtil.colorAlpha(new Color(67108863, true), (int)(122.4 + (animationFinished ? 132.6 * this.animationEnabled : (double)0.0F))));
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.floatSetting.getName()).color(colorText).size(7.5F).thickness(0.05F).build();
      text.render(matrix, x, y + (double)2.5F);
      float xValue = (float)(x + width - (double)((MsdfFont)FontManager.MONTSERRAT.get()).getWidth("" + this.floatSetting.getValue(), 8.0F));
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("" + this.floatSetting.getValue()).color(ColorUtil.setAlpha(this.animation, new Color(1040187391, true))).size(7.5F).thickness(0.05F).build();
      text.render(matrix, (double)xValue, y + (double)2.5F);
      if (animationFinished) {
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("" + this.floatSetting.getValue()).color(ColorUtil.setAlpha(this.animation * this.animationEnabled, CrolClient.INSTANCE.getThemeManager().getTheme().color())).size(7.5F).thickness(0.05F).build();
         text.render(matrix, (double)xValue, y + (double)2.5F);
      }

      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, (double)4.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(150994943, true)))).radius(new QuadRadiusState((double)1.5F)).smoothness(1.15F).build();
      rectangle.render(matrix, x, y + height - (double)5.0F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width * (double)this.floatSetting.getAnimationValue(), (double)4.0F)).color(new QuadColorState(new Color(704643071, true))).radius(new QuadRadiusState((double)1.5F)).smoothness(1.15F).build();
      rectangle.render(matrix, x, y + height - (double)5.0F);
      if (animationFinished) {
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width * (double)this.floatSetting.getAnimationValue(), (double)4.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation * this.animationEnabled, CrolClient.INSTANCE.getThemeManager().getTheme().color()))).radius(new QuadRadiusState((double)1.5F)).smoothness(1.15F).build();
         rectangle.render(matrix, x, y + height - (double)5.0F);
      }

      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(6.0F, 6.0F)).color(new QuadColorState(colorText)).radius(new QuadRadiusState(1.4)).smoothness(1.15F).build();
      rectangle.render(matrix, x + Math.max(width * (double)this.floatSetting.getAnimationValue() - (double)6.0F, (double)0.0F), y + height - (double)6.0F);
   }

   public void button(double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      this.render(x, y, width, height, mouseX, mouseY, drawContext);
      if (MouseUtil.isHovered(x, y, width, height, mouseX, mouseY) && MouseUtil.isHovered2(xStart, yStart, xEnd, yEnd, mouseX, mouseY)) {
         this.mouseClick(click);
         if (this.floatSetting.isSlide()) {
            this.floatSetting.setValue((float)MathHelper.clamp(MathUtil.round((double)((float)((mouseX - x) * (double)(this.floatSetting.getMaxValue() - this.floatSetting.getMinValue()) / width + (double)this.floatSetting.getMinValue())), (double)this.floatSetting.getIncriment()), (double)this.floatSetting.getMinValue(), (double)this.floatSetting.getMaxValue()));
         }
      }

   }

   public void setFloatSetting(FloatSetting floatSetting) {
      this.floatSetting = floatSetting;
   }

   public void setAnimationEnabled(double animationEnabled) {
      this.animationEnabled = animationEnabled;
   }

   public void setAnimation(double animation) {
      this.animation = animation;
   }
}
