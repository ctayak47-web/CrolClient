package crol.client.ui.mainmenu.button;

import crol.client.managers.FontManager;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
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
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class Button {
   private double x;
   private double y;
   private double width = (double)140.0F;
   private double height = (double)28.0F;
   private Runnable run;
   private String name;
   private String icon;
   private Animation animation;

   public Button(String name, String icon, Runnable run) {
      this.run = run;
      this.name = name;
      this.icon = icon;
      this.animation = new EaseBackIn(250, (double)1.0F, 0.1F, Direction.FORWARDS);
   }

   public void isHovered(double mouseX, double mouseY) {
      if (MouseUtil.isHovered(this.x, this.y, this.width, this.height, mouseX, mouseY)) {
         this.animation.setDirection(Direction.FORWARDS);
      } else {
         this.animation.setDirection(Direction.BACKWARDS);
      }

   }

   public void onClick() {
      this.run.run();
   }

   public void render(Matrix4f matrix4f) {
      ((BuiltRectangle)Builder.rectangle().size(new SizeState(this.width, this.height)).color(new QuadColorState(new Color(-15000539, true))).radius(new QuadRadiusState(8.6)).smoothness(1.15F).build()).render(matrix4f, this.x, this.y);
      ((BuiltBorder)Builder.border().size(new SizeState(this.width, this.height)).color(new QuadColorState(new Color(2040108))).radius(new QuadRadiusState(8.6)).thickness(0.01F).smoothness(0.6F, 0.6F).build()).render(matrix4f, this.x, this.y);
      float widthIcon = ((MsdfFont)FontManager.MAINMENU.get()).getWidth(this.icon, 8.0F) + 6.0F;
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.MAINMENU.get()).text(this.icon).color(new Color(-9736840, true)).size(8.0F).thickness(0.05F).build()).render(matrix4f, this.x + this.width / (double)2.0F - (double)(((MsdfFont)FontManager.SUISSEINTREGULAR.get()).getWidth(this.getName(), 8.0F) / 2.0F) - (double)(widthIcon / 2.0F), this.y + (double)10.0F);
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTREGULAR.get()).text(this.getName()).color(new Color(-1191182337, true)).size(8.0F).thickness(0.05F).build()).render(matrix4f, this.x + this.width / (double)2.0F - (double)(((MsdfFont)FontManager.SUISSEINTREGULAR.get()).getWidth(this.getName(), 8.0F) / 2.0F) + (double)(widthIcon / 2.0F), this.y + (double)9.0F);
      if (!this.animation.finished(Direction.BACKWARDS)) {
         ((BuiltRectangle)Builder.rectangle().size(new SizeState(this.width, this.height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-14737108, true)))).radius(new QuadRadiusState(8.6)).smoothness(1.15F).build()).render(matrix4f, this.x, this.y);
         ((BuiltBorder)Builder.border().size(new SizeState(this.width, this.height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(2434866)))).radius(new QuadRadiusState(8.6)).thickness(0.01F).smoothness(0.6F, 0.6F).build()).render(matrix4f, this.x, this.y);
         ((BuiltText)Builder.text().font((MsdfFont)FontManager.MAINMENU.get()).text(this.icon).color(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-12536065, true))).size(8.0F).thickness(0.05F).build()).render(matrix4f, this.x + this.width / (double)2.0F - (double)(((MsdfFont)FontManager.SUISSEINTREGULAR.get()).getWidth(this.getName(), 8.0F) / 2.0F) - (double)(widthIcon / 2.0F), this.y + (double)10.0F);
         ((BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTREGULAR.get()).text(this.getName()).color(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1, true))).size(8.0F).thickness(0.05F).build()).render(matrix4f, this.x + this.width / (double)2.0F - (double)(((MsdfFont)FontManager.SUISSEINTREGULAR.get()).getWidth(this.getName(), 8.0F) / 2.0F) + (double)(widthIcon / 2.0F), this.y + (double)9.0F);
      }

   }

   public void updatePos(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public double getWidth() {
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }

   public String getName() {
      return this.name;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }
}
