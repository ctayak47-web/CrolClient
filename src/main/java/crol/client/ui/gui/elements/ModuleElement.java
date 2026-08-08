package crol.client.ui.gui.elements;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.Module;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.KeySetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.modules.settings.impl.StringSetting;
import crol.client.ui.gui.IElementable;
import crol.client.ui.gui.IKeyPressible;
import crol.client.ui.gui.IMouseReleable;
import crol.client.util.animations.Direction;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.math.MouseUtil;
import crol.client.util.render.Scissor;
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
public class ModuleElement implements IElementable, IMouseReleable, IKeyPressible {
   private Module module;
   private double animation;
   private final BooleanElement booleanElement = new BooleanElement();
   private final FloatElement floatElement = new FloatElement();
   private final ModeElement modeElement = new ModeElement();
   private final MultiBoxElement multiBoxElement = new MultiBoxElement();
   private final StringElement stringElement = new StringElement();
   private final KeyElement keyElement = new KeyElement();
   private int key = 0;

   public void mouseClick(int click) {
      switch (click) {
         case 0:
            this.module.setEnabled(!this.module.isEnabled());
            break;
         case 1:
            if (!this.module.getSettings().isEmpty()) {
               this.module.setOpened(!this.module.isOpened());
            }
            break;
         case 2:
            for(Module m : CrolClient.INSTANCE.getModuleManager().getModules()) {
               m.setBinded(false);
            }

            this.module.setBinded(true);
      }

   }

   public void render(double x, double y, double width, double height, double mouseX, double mouseY, DrawContext drawContext) {
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      boolean animationFinished = !this.module.getAnimation().finished(Direction.BACKWARDS);
      Color color = ColorUtil.setAlpha(this.animation, ColorUtil.colorAlpha(new Color(67108863, true), (int)((double)1.0F + (animationFinished ? (double)1.0F * this.module.getAnimation().getOutput() : (double)0.0F))));
      Color colorText = ColorUtil.setAlpha(this.animation, ColorUtil.colorAlpha(new Color(-1, true), (int)(122.4 + (animationFinished ? 132.6 * this.module.getAnimation().getOutput() : (double)0.0F))));
      float round = this.module.isOpened() ? 0.0F : 5.0F;
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(new Color(67108863, true))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x, y);
      BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.colorAlpha(new Color(67108863, true), 3))).radius(new QuadRadiusState(5.0F)).thickness(0.01F).smoothness(0.65F, 0.65F).build();
      border.render(matrix, x, y);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, this.module.getDefaultHeight())).color(new QuadColorState(color.brighter().brighter(), color.brighter().brighter(), color.darker().darker(), color.darker().darker())).radius(new QuadRadiusState(5.0F, round, round, 5.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x, y);
      if (animationFinished) {
         Color color1 = ColorUtil.setAlpha(this.animation * this.module.getAnimation().getOutput() * 0.05, CrolClient.INSTANCE.getThemeManager().getTheme().color());
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, this.module.getDefaultHeight())).color(new QuadColorState(new Color(83886081, true), new Color(83886081, true), color1, color1)).radius(new QuadRadiusState(5.0F, round, round, 5.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, x, y);
      }

      border = (BuiltBorder)Builder.border().size(new SizeState(width, this.module.getDefaultHeight())).color(new QuadColorState(ColorUtil.colorAlpha(new Color(67108863, true), (int)((double)3.0F + (animationFinished ? (double)2.0F * this.module.getAnimation().getOutput() : (double)0.0F))))).radius(new QuadRadiusState(5.0F, round, round, 5.0F)).thickness(0.01F).smoothness(0.65F, 0.65F).build();
      border.render(matrix, x, y);
      if (animationFinished) {
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(1.0F, 8.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.module.getAnimation().getOutput() * this.animation, CrolClient.INSTANCE.getThemeManager().getTheme().color()))).radius(new QuadRadiusState((double)0.5F)).smoothness(0.1F).build();
         rectangle.render(matrix, x + (double)5.0F + this.module.getAnimation().getOutput() * (double)5.0F, y + (double)9.5F);
      }

      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.module.isBinded() ? "Press key" : this.module.getModuleInfo().name()).color(colorText).size(7.5F).thickness(0.05F).build();
      text.render(matrix, x + (double)9.0F + this.module.getAnimation().getOutput() * (double)5.0F, y + (double)9.0F);
      Color colorDesc = ColorUtil.setAlpha(this.animation, new Color(1040187391, true));
      Color colorDescEnabled = ColorUtil.setAlpha(this.animation * this.module.getAnimation().getOutput(), new Color(2063597567, true));
      Color colorImageEnable = ColorUtil.setAlpha(this.animation * this.module.getAnimation().getOutput(), new Color(1040187391, true));
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTREGULAR.get()).text(this.module.getDesc()[0]).color(colorDesc).size(5.8F).thickness(0.05F).build();
      text.render(matrix, x + (double)9.0F, y + (double)23.5F);
      if (this.module.isDoubleDesc()) {
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTREGULAR.get()).text(this.module.getDesc()[1]).color(colorDesc).size(5.8F).thickness(0.05F).build();
         text.render(matrix, x + (double)9.0F, y + (double)30.5F);
      }

      if (animationFinished) {
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTREGULAR.get()).text(this.module.getDesc()[0]).color(colorDescEnabled).size(5.8F).thickness(0.05F).build();
         text.render(matrix, x + (double)9.0F, y + (double)23.5F);
         if (this.module.isDoubleDesc()) {
            text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTREGULAR.get()).text(this.module.getDesc()[1]).color(colorDescEnabled).size(5.8F).thickness(0.05F).build();
            text.render(matrix, x + (double)9.0F, y + (double)30.5F);
         }
      }

      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)16.5F, (double)9.5F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(704643071, true)))).radius(new QuadRadiusState(3.8)).smoothness(1.15F).build();
      rectangle.render(matrix, x + width - (double)25.5F, y + (double)8.5F);
      if (animationFinished) {
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)16.5F, (double)9.5F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation * this.module.getAnimation().getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color()))).radius(new QuadRadiusState(3.8)).smoothness(1.15F).build();
         rectangle.render(matrix, x + width - (double)25.5F, y + (double)8.5F);
      }

      double roundX = x + width - (double)24.25F + this.module.getAnimation().getOutput() * (double)6.5F;
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)7.5F, (double)7.5F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation, new Color(2063597567, true)))).radius(new QuadRadiusState(2.8)).smoothness(1.15F).build();
      rectangle.render(matrix, roundX, y + (double)9.5F);
      if (animationFinished) {
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)7.5F, (double)7.5F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation * this.module.getAnimation().getOutput(), Color.WHITE))).radius(new QuadRadiusState(2.8)).smoothness(1.15F).build();
         rectangle.render(matrix, roundX, y + (double)9.5F);
      }

      boolean isEmpty = !this.module.getSettings().isEmpty();
      if (isEmpty) {
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.CATEGORY.get()).text("E").color(ColorUtil.setAlpha(this.animation, new Color(1040187391, true))).size(7.75F).thickness(0.05F).build();
         text.render(matrix, x + width - (double)40.0F, y + (double)9.25F);
      }

      double xBind = width - (double)23.0F - (double)((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(this.module.getNameBind(), 5.8F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.module.getNameBind()).color(colorDesc).size(5.8F).thickness(0.05F).build();
      text.render(matrix, x + xBind, y + (double)23.5F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("R").color(ColorUtil.setAlpha(this.animation, new Color(536870911, true))).size(5.3F).thickness(0.05F).build();
      text.render(matrix, x + width - (double)16.0F, y + 23.4);
      if (animationFinished) {
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.module.getNameBind()).color(colorDescEnabled).size(5.8F).thickness(0.05F).build();
         text.render(matrix, x + xBind, y + (double)23.5F);
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("R").color(ColorUtil.setAlpha(this.animation * this.module.getAnimation().getOutput(), colorImageEnable)).size(5.3F).thickness(0.05F).build();
         text.render(matrix, x + width - (double)16.0F, y + 23.4);
      }

      if (!this.module.getAnimSettings().finished(Direction.BACKWARDS)) {
         if (isEmpty) {
            text = (BuiltText)Builder.text().font((MsdfFont)FontManager.CATEGORY.get()).text("E").color(ColorUtil.setAlpha(this.animation * this.module.getAnimSettings().getOutput(), new Color(2063597567, true))).size(7.75F).thickness(0.05F).build();
            text.render(matrix, x + width - (double)40.0F, y + (double)9.25F);
         }

         if (animationFinished) {
            text = (BuiltText)Builder.text().font((MsdfFont)FontManager.CATEGORY.get()).text("E").color(ColorUtil.setAlpha(this.animation * this.module.getAnimSettings().getOutput() * this.module.getAnimation().getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color())).size(7.75F).thickness(0.05F).build();
            text.render(matrix, x + width - (double)40.0F, y + (double)9.25F);
         }
      }

   }

   public void button(double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      this.render(x, y, width, height, mouseX, mouseY, drawContext);
      if (MouseUtil.isHovered(x, y, width, this.module.getDefaultHeight(), mouseX, mouseY) && MouseUtil.isHovered2(xStart, yStart, xEnd, yEnd, mouseX, mouseY)) {
         this.mouseClick(click);
      }

      float value = Math.max(0.0F, (float)this.module.getHeight() - 42.0F);
      if (this.module.isOpened()) {
         Scissor.StartScissor((float)(x + (double)9.0F), (float)y, (float)(width - (double)18.0F), (float)this.module.getHeight() - 3.8F);
         this.renderSettings(x + (double)9.0F, y + (double)39.5F, width - (double)18.0F, mouseX, mouseY, xStart, yStart, xEnd, yEnd, drawContext, click);
         Scissor.stopScissor();
      } else {
         this.module.setHeight((double)MathUtil.fast((float)this.module.getHeight(), (float)this.module.getDefaultHeight(), 10.0F));
      }

   }

   private void renderSettings(double x, double y, double width, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      double modulAnimation = this.module.getAnimation().getOutput();
      this.booleanElement.setAnimation(this.animation);
      this.booleanElement.setAnimationEnabled(modulAnimation);
      this.floatElement.setAnimation(this.animation);
      this.floatElement.setAnimationEnabled(modulAnimation);
      this.modeElement.setAnimation(this.animation);
      this.modeElement.setAnimationEnabled(modulAnimation);
      this.multiBoxElement.setAnimation(this.animation);
      this.multiBoxElement.setAnimationEnabled(modulAnimation);
      this.keyElement.setAnimation(this.animation);
      this.keyElement.setKey(this.key);
      this.keyElement.setAnimationEnabled(modulAnimation);
      float offset = this.module.getDefaultHeight() == (double)38.5F ? 4.0F : 10.5F;

      for(Setting setting : this.module.getSettings()) {
         if (setting.isVisible()) {
            if (setting instanceof BooleanSetting) {
               BooleanSetting booleanSetting = (BooleanSetting)setting;
               this.booleanElement.setBooleanSetting(booleanSetting);
               this.booleanElement.button(x, y + (double)offset, width, (double)15.0F, mouseX, mouseY, xStart, yStart, xEnd, yEnd, drawContext, click);
               offset += 19.0F;
            } else if (setting instanceof FloatSetting) {
               FloatSetting floatSetting = (FloatSetting)setting;
               this.floatElement.setFloatSetting(floatSetting);
               this.floatElement.button(x, y + (double)offset, width, (double)19.0F, mouseX, mouseY, xStart, yStart, xEnd, yEnd, drawContext, click);
               offset += 23.0F;
            } else if (setting instanceof ModeSetting) {
               ModeSetting modeSetting = (ModeSetting)setting;
               this.modeElement.setModeSetting(modeSetting);
               this.modeElement.button(x, y + (double)offset, width, (double)15.0F, mouseX, mouseY, xStart, yStart, xEnd, yEnd, drawContext, click);
               offset += 36.0F + modeSetting.getOffset();
            } else if (setting instanceof MultiBoxSetting) {
               MultiBoxSetting multiBoxSetting = (MultiBoxSetting)setting;
               this.multiBoxElement.setMultiBoxSetting(multiBoxSetting);
               this.multiBoxElement.button(x, y + (double)offset, width, (double)15.0F, mouseX, mouseY, xStart, yStart, xEnd, yEnd, drawContext, click);
               offset += 36.0F + multiBoxSetting.getOffset();
            } else if (setting instanceof StringSetting) {
               StringSetting stringSetting = (StringSetting)setting;
            } else if (setting instanceof KeySetting) {
               KeySetting keySetting = (KeySetting)setting;
               this.keyElement.setKeySetting(keySetting);
               this.keyElement.button(x, y + (double)offset, width, (double)18.0F, mouseX, mouseY, xStart, yStart, xEnd, yEnd, drawContext, click);
               offset += 19.0F;
            }
         }
      }

      this.module.setHeight((double)MathUtil.fast((float)this.module.getHeight(), (float)((double)offset + this.module.getDefaultHeight() + (double)5.0F - (double)(this.module.getDefaultHeight() == (double)38.5F ? 4.0F : 10.5F)), 10.0F));
   }

   public void setAnimation(double animation) {
      this.animation = animation;
   }

   public void setModule(Module module) {
      this.module = module;
   }

   public void onMouseRelease() {
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
   }

   public void setKey(int key) {
      this.key = key;
   }
}
