package crol.client.ui.gui.elements;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.Category;
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
public class CategoryElement implements IElementable {
   private Category category;
   private double animation;

   public void mouseClick(int click) {
      if (click == 0) {
         CrolClient.INSTANCE.getGui().getCategory().getAnimation().setDirection(Direction.BACKWARDS);
         this.category.getAnimation().setDirection(Direction.FORWARDS);
         CrolClient.INSTANCE.getGui().setCategory(this.category);
         CrolClient.INSTANCE.getGui().updateModules();
         CrolClient.INSTANCE.getGui().setScroll((double)0.0F);
      }

   }

   public void render(double x, double y, double width, double height, double mouseX, double mouseY, DrawContext drawContext) {
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      boolean isCategory = this.category == CrolClient.INSTANCE.getGui().getCategory();
      if (isCategory) {
         BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(100663295, true)))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, x, y);
         BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(184549375, true)))).radius(new QuadRadiusState(5.0F)).thickness(0.01F).smoothness(0.6F, 0.6F).build();
         border.render(matrix, x, y);
      } else {
         BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(67108863, true)))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, x, y);
         BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(150994943, true)))).radius(new QuadRadiusState(5.0F)).thickness(0.01F).smoothness(0.6F, 0.6F).build();
         border.render(matrix, x, y);
      }

      Color color = ColorUtil.setAlpha(this.animation, (new Color(-432260036, true)).brighter());
      BuiltText icon = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text(this.category.getIcon()).color(color).size(10.0F).thickness(0.05F).build();
      icon.render(matrix, x + 6.8 + (this.category == Category.PLAYER ? (double)1.5F : (double)0.0F), y + (double)7.0F);
      if (!this.category.getAnimation().finished(Direction.BACKWARDS)) {
         Color colorEnd = ColorUtil.setAlpha(this.animation * this.category.getAnimation().getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color());
         icon = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text(this.category.getIcon()).color(colorEnd).size(10.0F).thickness(0.05F).build();
         icon.render(matrix, x + (double)6.5F + (this.category == Category.PLAYER ? (double)1.5F : (double)0.0F), y + (double)7.0F);
      }

   }

   public void button(double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      this.render(x, y, width, height, mouseX, mouseY, drawContext);
      if (MouseUtil.isHovered(x, y, width, height, mouseX, mouseY)) {
         this.mouseClick(click);
      }

   }

   public void setCategory(Category category) {
      this.category = category;
   }

   public void setAnimation(double animation) {
      this.animation = animation;
   }
}
