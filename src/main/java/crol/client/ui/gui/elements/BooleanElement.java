package crol.client.ui.gui.elements;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.settings.impl.BooleanSetting;
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
public class BooleanElement implements IElementable {
   private BooleanSetting booleanSetting;
   private double animation;
   private double animationEnabled;

   public void mouseClick(int click) {
      if (click == 0) {
         this.booleanSetting.setValue(!this.booleanSetting.getValue());
      }

   }

   public void render(double x, double y, double width, double height, double mouseX, double mouseY, DrawContext drawContext) {
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      boolean animationFinished = this.animationEnabled > (double)0.0F;
      Color colorText = ColorUtil.setAlpha(this.animation, ColorUtil.colorAlpha(new Color(67108863, true), (int)(122.4 + (animationFinished ? 132.6 * this.animationEnabled : (double)0.0F))));
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.booleanSetting.getName()).color(colorText).size(7.5F).thickness(0.05F).build();
      text.render(matrix, x, y + (double)2.5F);
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)8.5F, (double)8.5F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(67108863, true)))).radius(new QuadRadiusState(2.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x + width - (double)8.5F, y + (double)2.5F);
      BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState((double)8.5F, (double)8.5F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(184549375, true)))).radius(new QuadRadiusState(2.0F)).thickness(0.01F).smoothness(0.65F, 0.65F).build();
      border.render(matrix, x + width - (double)8.5F, y + (double)2.5F);
      if (!this.booleanSetting.getAnimation().finished(Direction.BACKWARDS)) {
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)8.5F, (double)8.5F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation * this.booleanSetting.getAnimation().getOutput(), new Color(704643071, true)))).radius(new QuadRadiusState(2.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, x + width - (double)8.5F, y + (double)2.5F);
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("L").color(ColorUtil.setAlpha(this.animation * this.booleanSetting.getAnimation().getOutput(), new Color(2063597567, true))).size(4.25F).thickness(0.05F).build();
         text.render(matrix, x + width - 6.9, y + (double)4.25F);
         if (animationFinished) {
            rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)8.5F, (double)8.5F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation * this.animationEnabled * this.booleanSetting.getAnimation().getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color()))).radius(new QuadRadiusState(2.0F)).smoothness(1.15F).build();
            rectangle.render(matrix, x + width - (double)8.5F, y + (double)2.5F);
            text = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("L").color(ColorUtil.setAlpha(this.animation * this.animationEnabled * this.booleanSetting.getAnimation().getOutput(), Color.WHITE)).size(4.25F).thickness(0.05F).build();
            text.render(matrix, x + width - 6.9, y + (double)4.25F);
         }
      }

   }

   public void button(double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      this.render(x, y, width, height, mouseX, mouseY, drawContext);
      if (MouseUtil.isHovered(x, y, width, height, mouseX, mouseY) && MouseUtil.isHovered2(xStart, yStart, xEnd, yEnd, mouseX, mouseY)) {
         this.mouseClick(click);
      }

   }

   public void setAnimation(double animation) {
      this.animation = animation;
   }

   public void setAnimationEnabled(double animationEnabled) {
      this.animationEnabled = animationEnabled;
   }

   public void setBooleanSetting(BooleanSetting booleanSetting) {
      this.booleanSetting = booleanSetting;
   }
}
