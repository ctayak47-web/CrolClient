package crol.client.ui.hud.impl;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.Module;
import crol.client.ui.draggable.impl.BindsDraggable;
import crol.client.ui.hud.HudElement;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBlur;
import crol.client.util.render.renderers.impl.BuiltBorder;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BindsElement extends HudElement {
   private float height = 0.0F;
   private float offset = 0.0F;
   private final Animation animation;

   public BindsElement() {
      super(new BindsDraggable(), "Potions");
      this.animation = new EaseBackIn(300, (double)1.0F, 0.1F, Direction.BACKWARDS);
   }

   public void render(DrawContext drawContext) {
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      double width = (double)100.0F;
      Color white = ColorUtil.setAlpha(this.animation.getOutput(), Color.WHITE);
      Color blackN = ColorUtil.setAlpha(this.animation.getOutput(), new Color(2048202266, true));
      Color black2 = ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1206577638, true));
      Color clientColor = ColorUtil.setAlpha(this.animation.getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color());
      BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(width, (double)(28.0F + this.height))).radius(new QuadRadiusState(8.0F)).blurRadius(12.0F).smoothness(1.0F).color(new QuadColorState(white)).build();
      blur.render(matrix, (float)this.draggable.x, (float)this.draggable.y);
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, (double)(28.0F + this.height))).color(new QuadColorState(blackN)).radius(new QuadRadiusState(8.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)this.draggable.x, (float)this.draggable.y);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width - (double)8.0F, (double)18.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(100663295, true)))).radius(new QuadRadiusState(5.0F, 2.0F, 2.0F, 5.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)(this.draggable.x + 4), (float)(this.draggable.y + 4));
      BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(width - (double)8.0F, (double)18.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(184549375, true)))).radius(new QuadRadiusState(5.0F, 2.0F, 2.0F, 5.0F)).thickness(0.01F).smoothness(0.65F, 0.65F).build();
      border.render(matrix, (float)(this.draggable.x + 4), (float)(this.draggable.y + 4));
      BuiltText themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("Binds").color(white).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (double)(this.draggable.x + 10), (double)this.draggable.y + (double)8.5F);
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("D").color(white).size(10.0F).thickness(0.05F).build();
      themeText.render(matrix, (double)this.draggable.x + width - (double)21.0F, (double)this.draggable.y + (double)7.5F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width - (double)30.0F, (double)this.height)).color(new QuadColorState(black2)).radius(new QuadRadiusState(3.0F, 5.0F, 3.0F, 3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)(this.draggable.x + 4), (float)(this.draggable.y + 24));
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(20.0F, this.height)).color(new QuadColorState(black2)).radius(new QuadRadiusState(3.0F, 3.0F, 5.0F, 3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)(this.draggable.x + 76), (float)(this.draggable.y + 24));
      this.offset = 0.0F;

      for(Module m : CrolClient.INSTANCE.getModuleManager().getModules()) {
         if (m.isEnabled() && !m.getNameBind().equals("n/a") && this.offset - 1.0F < this.height - 4.0F) {
            Color clientColor2 = ColorUtil.setAlpha(m.getAnimation().getOutput(), clientColor);
            rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(1.0F, 8.0F)).color(new QuadColorState(clientColor2)).radius(new QuadRadiusState((double)0.5F)).smoothness(0.1F).build();
            rectangle.render(matrix, (double)this.draggable.x + (double)8.5F, (double)this.draggable.y + (double)28.25F + (double)this.offset);
            themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(m.getModuleInfo().name()).color(ColorUtil.setAlpha(m.getAnimation().getOutput(), white)).size(7.2F).thickness(0.05F).build();
            themeText.render(matrix, (double)this.draggable.x + (double)12.5F, (double)((float)(this.draggable.y + 28) + this.offset));
            themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(m.getNameBind()).color(clientColor2).size(7.2F).thickness(0.05F).build();
            themeText.render(matrix, (float)(this.draggable.x + 76 + 10) - ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(m.getNameBind(), 7.2F) / 2.0F, (float)(this.draggable.y + 28) + this.offset);
         }

         if (m.isEnabled() && !m.getNameBind().equals("n/a")) {
            this.offset = (float)((double)this.offset + (double)13.0F * m.getAnimation().getOutput());
         }
      }

      this.height = MathUtil.fast(this.height, this.offset + 3.0F, 10.0F);
      if (this.offset > 1.0F) {
         this.animation.setDirection(Direction.FORWARDS);
      } else {
         this.animation.setDirection(Direction.BACKWARDS);
      }

   }
}
