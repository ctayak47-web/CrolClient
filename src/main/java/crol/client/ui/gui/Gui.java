package crol.client.ui.gui;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.impl.render.GuiModule;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.ui.gui.elements.CategoryElement;
import crol.client.ui.gui.elements.ModuleElement;
import crol.client.ui.gui.elements.SearchElement;
import crol.client.ui.themes.Theme;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.math.MouseUtil;
import crol.client.util.render.Scissor;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBlur;
import crol.client.util.render.renderers.impl.BuiltBorder;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import crol.client.util.render.renderers.impl.BuiltTexture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class Gui extends Screen {
   private double x;
   private double y;
   private double widthGui;
   private double heightGui;
   private double r;
   private final Animation animation;
   public int preScroll;
   public int mainScroll;
   private final CategoryElement categoryElement;
   private final ModuleElement moduleElement;
   private final SearchElement searchElement;
   private Category category;
   private int key;
   private String type = "";
   private double offsetX;
   private double offsetY;
   private double offsetY2;
   private int click;
   private final List<Module> modules;

   public Gui() {
      super(Text.of(""));
      this.animation = new EaseBackIn(355, (double)1.0F, 0.1F, Direction.BACKWARDS);
      this.categoryElement = new CategoryElement();
      this.moduleElement = new ModuleElement();
      this.modules = new ArrayList();
      this.searchElement = new SearchElement();
      this.category = Category.COMBAT;
      this.offsetX = (double)0.0F;
      this.offsetY = (double)0.0F;
      this.offsetY2 = (double)0.0F;
      this.click = -1;
      Category.COMBAT.getAnimation().setDirection(Direction.FORWARDS);
      this.updateModules();
      this.preScroll = 0;
      this.mainScroll = 0;
   }

   public void updateModules() {
      this.modules.clear();
      this.modules.addAll(CrolClient.INSTANCE.getModuleManager().getByCategory(this.category));
   }

   @Compile
   protected void init() {
      this.animation.setDirection(Direction.FORWARDS);
      this.widthGui = (double)450.0F;
      this.heightGui = (double)300.0F;
      this.x = (double)(this.width / 2) - this.widthGui / (double)2.0F;
      this.y = (double)(this.height / 2) - this.heightGui / (double)2.0F + (double)25.0F;
      this.r = (double)11.0F;
      super.init();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
      Color white = ColorUtil.setAlpha(this.animation.getOutput(), Color.white);
      Color logoColor = ColorUtil.setAlpha(this.animation.getOutput(), (new Color(-12829636, true)).brighter());
      Color nameColor = ColorUtil.setAlpha(this.animation.getOutput(), Color.gray);
      Color lineColor = ColorUtil.setAlpha(this.animation.getOutput(), new Color(1040187391, true));
      this.y -= (double)13.0F;
      BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(200.0F, 98.0F)).radius(new QuadRadiusState(this.r)).blurRadius((float)((double)18.0F * this.animation.getOutput())).smoothness((float)((double)1.0F * this.animation.getOutput())).color(new QuadColorState(Color.white)).build();
      blur.render(matrix4f, this.x, this.y - (double)95.0F);
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(200.0F, 98.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(2048202266, true)))).radius(new QuadRadiusState(this.r)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x, this.y - (double)95.0F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(190.0F, 30.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1206446313, true)))).radius(new QuadRadiusState(this.r, (double)2.0F, (double)2.0F, this.r)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)5.0F, this.y - (double)90.0F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(190.0F, 55.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1206446313, true)))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)5.0F, this.y - (double)57.0F);
      Color color = ColorUtil.setAlpha(this.animation.getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color());
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(20.0F, 20.0F)).color(new QuadColorState(color)).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)10.0F, this.y - (double)85.0F);
      BuiltText themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("S").color(white).size(9.0F).thickness(0.05F).build();
      themeText.render(matrix4f, this.x + (double)15.0F, this.y - (double)80.0F);
      BuiltText icon = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("A").color(logoColor).size(7.4F).thickness(0.05F).build();
      icon.render(matrix4f, this.x + (double)40.0F, this.y - (double)80.0F);
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("wildclient.org").color(nameColor).size(8.0F).thickness(0.05F).build();
      themeText.render(matrix4f, this.x + (double)51.0F, this.y - (double)80.5F);
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("T").color(lineColor).size(5.7F).thickness(0.05F).build();
      themeText.render(matrix4f, this.x + (double)114.0F, this.y - (double)79.0F);
      icon = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("S").color(color).size(8.0F).thickness(0.05F).build();
      icon.render(matrix4f, this.x + (double)126.0F - (double)0.5F, this.y - (double)80.5F);
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("Themes").color(white).size(8.0F).thickness(0.05F).build();
      themeText.render(matrix4f, this.x + (double)139.0F, this.y - (double)80.5F);
      this.offsetY = (double)0.0F;
      this.offsetX = (double)0.0F;

      for(Theme theme : CrolClient.INSTANCE.getThemeManager().getThemes()) {
         this.themeButton(this.x + (double)10.0F + this.offsetX, this.y - (double)52.5F + this.offsetY, (double)22.0F, (double)22.0F, (double)mouseX, (double)mouseY, theme, matrix4f);
         this.offsetX += 26.1;
         if (this.offsetX > (double)160.0F) {
            this.offsetY += (double)24.0F;
            this.offsetX = (double)0.0F;
         }
      }

      this.y += (double)13.0F;
      blur = (BuiltBlur)Builder.blur().size(new SizeState(this.widthGui, this.heightGui)).radius(new QuadRadiusState(this.r)).blurRadius((float)((double)18.0F * this.animation.getOutput())).smoothness((float)((double)1.0F * this.animation.getOutput())).color(new QuadColorState(Color.white)).build();
      blur.render(matrix4f, this.x, this.y);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(this.widthGui, this.heightGui)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(2048202266, true)))).radius(new QuadRadiusState(this.r)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x, this.y);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)45.0F, this.heightGui - (double)10.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1206446313, true)))).radius(new QuadRadiusState(this.r, this.r, (double)3.0F, (double)3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)5.0F, this.y + (double)5.0F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(this.widthGui - (double)50.0F - (double)150.0F, (double)30.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1206446313, true)))).radius(new QuadRadiusState(3.0F, 3.0F, 3.0F, 3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)55.0F, this.y + (double)5.0F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(135.0F, 30.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1206446313, true)))).radius(new QuadRadiusState((double)3.0F, (double)3.0F, (double)3.0F, this.r)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)60.0F + (this.widthGui - (double)50.0F - (double)150.0F), this.y + (double)5.0F);
      this.searchElement.setAnimation(this.animation.getOutput());
      this.searchElement.setKey(this.key);
      this.searchElement.setType(this.type);
      this.searchElement.button(this.x + (double)60.0F + (this.widthGui - (double)50.0F - (double)150.0F), this.y + (double)5.0F, (double)135.0F, (double)30.0F, (double)mouseX, (double)mouseY, this.x, this.y, this.widthGui, this.heightGui, context, this.click);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(this.widthGui - (double)60.0F, this.heightGui - (double)44.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1206446313, true)))).radius(new QuadRadiusState((double)3.0F, (double)3.0F, this.r, (double)3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)55.0F, this.y + (double)39.0F);
      icon = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("A").color(logoColor).size(7.4F).thickness(0.05F).build();
      icon.render(matrix4f, this.x + (double)63.0F, this.y + (double)15.5F);
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("wildclient.org").color(nameColor).size(8.0F).thickness(0.05F).build();
      text.render(matrix4f, this.x + (double)74.0F, this.y + (double)15.0F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("T").color(lineColor).size(5.7F).thickness(0.05F).build();
      text.render(matrix4f, this.x + (double)137.0F, this.y + (double)16.5F);
      icon = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text(this.category.getIcon()).color(color).size(8.0F).thickness(0.05F).build();
      icon.render(matrix4f, this.x + (double)149.0F + (this.category == Category.PLAYER ? (double)1.5F : (double)0.0F), this.y + (double)15.0F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.category.getName()).color(white).size(8.0F).thickness(0.05F).build();
      text.render(matrix4f, this.x + (double)162.0F, this.y + (double)15.0F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(27.0F, 27.0F)).color(new QuadColorState(color)).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)5.0F + (double)22.5F - (double)13.5F, this.y + (double)13.0F);
      icon = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("A").color(white).size(18.0F).thickness(0.05F).build();
      icon.render(matrix4f, this.x + (double)8.0F + (double)22.5F - (double)13.5F, this.y + (double)16.0F);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)27.0F, 1.9)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(352321535, true)))).radius(new QuadRadiusState(4.0F)).smoothness(1.15F).build();
      rectangle.render(matrix4f, this.x + (double)13.5F, this.y + (double)48.5F);
      this.offsetY = (double)0.0F;
      double xCategory = this.x + (double)5.0F + (double)22.5F - (double)12.5F;

      for(Category category : Category.values()) {
         this.categoryElement.setCategory(category);
         this.categoryElement.setAnimation(this.animation.getOutput());
         this.categoryElement.button(xCategory, this.y + (double)59.0F + this.offsetY, (double)25.0F, (double)25.0F, (double)mouseX, (double)mouseY, this.x, this.y, this.x + this.widthGui, this.y + this.heightGui, context, this.click);
         this.offsetY += (double)30.0F;
      }

      AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("crol", "images/gui/avatar.png"));
      BuiltTexture texture = (BuiltTexture)Builder.texture().size(new SizeState(25.0F, 25.0F)).radius(new QuadRadiusState(12.0F)).texture(0.0F, 0.0F, 1.0F, 1.0F, abstractTexture).color(new QuadColorState(white)).build();
      texture.render(matrix4f, (float)(this.x + (double)5.0F + (double)22.5F - (double)12.5F), (float)(this.y + this.heightGui - (double)42.0F), 0.0F);
      this.offsetX = (double)0.0F;
      this.offsetY = (double)0.0F;
      this.offsetY2 = (double)0.0F;
      this.moduleElement.setAnimation(this.animation.getOutput());
      this.moduleElement.setKey(this.key);
      double xModules = this.x + (double)65.0F;
      double yModules = this.y + (double)49.0F;
      double widthMoidule = (double)182.5F;
      context.getMatrices().push();
      Scissor.StartScissor((float)xModules, (float)yModules - 8.0F, 380.0F, 253.0F);
      if (this.searchElement.getValue().isEmpty()) {
         int yAnim = 0;

         for(Module module : this.modules) {
            this.moduleElement.setModule(module);
            if (this.offsetX == (double)0.0F) {
               this.moduleElement.button(xModules + this.offsetX, yModules + this.offsetY + (double)this.mainScroll + (double)yAnim, widthMoidule, module.getHeight(), (double)mouseX, (double)mouseY, this.x, yModules, this.x + this.widthGui, this.y + this.heightGui, context, this.click);
               this.offsetY += module.getHeight() + (double)5.0F;
               this.offsetX += widthMoidule + (double)5.0F;
            } else {
               this.moduleElement.button(xModules + this.offsetX, yModules + this.offsetY2 + (double)this.mainScroll + (double)yAnim, widthMoidule, module.getHeight(), (double)mouseX, (double)mouseY, this.x, yModules, this.x + this.widthGui, this.y + this.heightGui, context, this.click);
               this.offsetY2 += module.getHeight() + (double)5.0F;
               this.offsetX = (double)0.0F;
            }

            if (module.isBinded()) {
               for(int button = 0; button <= 7; ++button) {
                  if (GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), button) == 1 && button != 2 && button != 0) {
                     module.setBind(1330 + button);
                     module.setBinded(false);
                  }
               }
            }
         }
      } else {
         int yAnim = 0;

         for(Module module : CrolClient.INSTANCE.getModuleManager().search(this.searchElement.getValue())) {
            this.moduleElement.setModule(module);
            if (this.offsetX == (double)0.0F) {
               this.moduleElement.button(xModules + this.offsetX, yModules + this.offsetY + (double)this.mainScroll + (double)yAnim, widthMoidule, module.getHeight(), (double)mouseX, (double)mouseY, this.x, yModules, this.x + this.widthGui, this.y + this.heightGui, context, this.click);
               this.offsetY += module.getHeight() + (double)5.0F;
               this.offsetX += widthMoidule + (double)5.0F;
            } else {
               this.moduleElement.button(xModules + this.offsetX, yModules + this.offsetY2 + (double)this.mainScroll + (double)yAnim, widthMoidule, module.getHeight(), (double)mouseX, (double)mouseY, this.x, yModules, this.x + this.widthGui, this.y + this.heightGui, context, this.click);
               this.offsetY2 += module.getHeight() + (double)5.0F;
               this.offsetX = (double)0.0F;
            }

            if (module.isBinded()) {
               for(int button = 0; button <= 7; ++button) {
                  if (GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), button) == 1 && button != 2 && button != 0) {
                     module.setBind(1330 + button);
                     module.setBinded(false);
                  }
               }
            }
         }
      }

      Scissor.stopScissor();
      context.getMatrices().pop();
      double maxScroll = Math.max(this.offsetY, this.offsetY2) - (double)38.0F;
      if ((double)this.preScroll < -maxScroll) {
         this.preScroll = (int)MathUtil.fast((float)this.preScroll, (float)(-maxScroll), 1000.0F);
      } else if (this.preScroll > 0) {
         this.preScroll = (int)MathUtil.fast((float)this.preScroll, 0.0F, 1000.0F);
      }

      this.mainScroll = (int)MathUtil.fast((float)this.mainScroll, (float)this.preScroll, 13.0F);
      if (this.animation.finished(Direction.BACKWARDS)) {
         CrolClient.INSTANCE.getModuleManager().getByClass(GuiModule.class).setEnabled(false);
      }

      this.key = 0;
      this.type = "";
      this.click = -1;
   }

   private void themeButton(double x, double y, double width, double height, double mouseX, double mouseY, Theme theme, Matrix4f matrix) {
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(67108863, true)))).radius(new QuadRadiusState(3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x, y);
      BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(100663295, true)))).radius(new QuadRadiusState(4.0F)).thickness(0.01F).smoothness(0.6F, 0.6F).build();
      border.render(matrix, x, y);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width * 0.7, height * 0.7)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), theme.color()))).radius(new QuadRadiusState(4.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, x + width * 0.15, y + height * 0.15);
      if (MouseUtil.isHovered(x, y, width, height, mouseX, mouseY) && this.click == 0) {
         CrolClient.INSTANCE.getThemeManager().setTheme(theme);
      }

   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   public boolean charTyped(char chr, int modifiers) {
      this.type = String.valueOf(chr);
      return super.charTyped(chr, modifiers);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.modules.forEach((module) -> module.getSettings().stream().filter((setting) -> setting instanceof FloatSetting).forEach((setting) -> ((FloatSetting)setting).setSlide(false)));
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.key = keyCode;
      boolean bindKey = false;

      for(Module m : this.modules) {
         if (m != null && m.isBinded()) {
            int endKey = keyCode != 261 && keyCode != 259 && keyCode != 256 ? keyCode : -1;
            bindKey = true;
            m.setBind(endKey);
            m.setBinded(false);
         }
      }

      if (bindKey) {
         return false;
      } else if (keyCode != CrolClient.INSTANCE.getModuleManager().getByClass(GuiModule.class).getBind() && keyCode != 256) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         this.animation.setDirection(Direction.BACKWARDS);
         return false;
      }
   }

   public Category getCategory() {
      return this.category;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.searchElement.setSelect(false);
      this.click = button;
      this.key = 1330 + button;
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public void setScroll(double scroll) {
      this.mainScroll = (int)scroll;
      this.preScroll = (int)scroll;
   }

   public void setCategory(Category category) {
      this.category = category;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.preScroll += (int)(verticalAmount * (double)30.0F);
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }
}
