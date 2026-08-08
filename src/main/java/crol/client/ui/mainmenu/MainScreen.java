package crol.client.ui.mainmenu;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.ui.mainmenu.button.Button;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MouseUtil;
import crol.client.util.render.AnimationUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
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
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class MainScreen extends Screen {
   private final Button singlePlayerButton = new Button("Singleplayer", "Z", () -> MinecraftClient.getInstance().setScreen(new SelectWorldScreen(CrolClient.INSTANCE.getMainScreen())));
   private final Button multiPlayerButton = new Button("Multiplayer", "K", () -> MinecraftClient.getInstance().setScreen(new MultiplayerScreen(CrolClient.INSTANCE.getMainScreen())));
   private final Button altManagerButton = new Button("Altmanager", "A", () -> MinecraftClient.getInstance().setScreen(CrolClient.INSTANCE.getAltManagerScreen()));
   private final Button exitButton = new Button("Exit", "J", () -> MinecraftClient.getInstance().scheduleStop());
   private final Button options = new Button("Options", "H", () -> MinecraftClient.getInstance().setScreen(new OptionsScreen(CrolClient.INSTANCE.getMainScreen(), MinecraftClient.getInstance().options)));
   private final Animation animation;
   private final List<Button> buttons = new ArrayList();
   private boolean init = false;

   public MainScreen() {
      super(Text.of(""));
      this.animation = new EaseBackIn(400, (double)1.0F, 0.1F, Direction.FORWARDS);
   }

   @Compile
   protected void init() {
      this.animation.reset();
      this.animation.setDirection(Direction.FORWARDS);
      if (!this.init) {
         CrolClient.INSTANCE.getModuleManager().initDesc();

         try {
            CrolClient.INSTANCE.getConfigManager().loadConfig("default");
            CrolClient.INSTANCE.getConfigManager().loadDraggables();
            CrolClient.INSTANCE.getConfigManager().loadTheme();
            CrolClient.INSTANCE.getConfigManager().loadNickNames();
            MinecraftClient.getInstance().options.getGuiScale().setValue(2);
         } catch (Exception var3) {
         }

         this.init = true;
      }

      this.buttons.clear();
      this.buttons.add(this.exitButton);
      this.buttons.add(this.options);
      this.buttons.add(this.altManagerButton);
      this.buttons.add(this.multiPlayerButton);
      this.buttons.add(this.singlePlayerButton);

      for(int i = 0; i < this.buttons.size(); ++i) {
         Button button = (Button)this.buttons.get(i);
         button.updatePos((double)(this.width / 2) - button.getWidth() / (double)2.0F, (double)(this.height / 2) - button.getHeight() / (double)2.0F - (double)(i * 33) + (double)100.0F);
      }

      super.init();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
      AnimationUtil.sizeAnimation(context, (double)(this.width / 2), (double)(this.height / 2), (double)1.35F - this.animation.getOutput() * 0.35);
      ((BuiltRectangle)Builder.rectangle().size(new SizeState((float)this.width, (float)this.height)).color(new QuadColorState(new Color(-15395300, true))).radius(new QuadRadiusState(0.0F)).smoothness(1.15F).build()).render(matrix4f, 0.0F, 0.0F);
      AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("crol", "images/mainmenu/background.png"));
      ((BuiltTexture)Builder.texture().size(new SizeState((float)this.width, (float)this.height)).radius(new QuadRadiusState(0.0F)).texture(0.0F, 0.0F, 1.0F, 1.0F, abstractTexture).color(new QuadColorState(ColorUtil.setAlpha(0.45, Color.WHITE))).build()).render(matrix4f, 0.0F, 0.0F);
      ((BuiltTexture)Builder.texture().size(new SizeState(200.0F, 200.0F)).radius(new QuadRadiusState(0.0F)).texture(0.0F, 0.0F, 1.0F, 1.0F, MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("crol", "images/mainmenu/logo.png"))).color(new QuadColorState(Color.WHITE)).build()).render(matrix4f, (float)(this.width / 2 - 100), (float)(this.height / 2 - 230));
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("WILD CLIENT").color(new Color(1295156479, true)).size(8.0F).thickness(0.05F).build()).render(matrix4f, (float)(this.width / 2) - ((MsdfFont)FontManager.BOLD.get()).getWidth("WILD CLIENT", 8.0F) / 2.0F, (float)(this.height / 2 - 100 + 7));
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("VERSION 1.0").color(new Color(775062783, true)).size(7.8F).thickness(0.05F).build()).render(matrix4f, (float)(this.width / 2) - ((MsdfFont)FontManager.BOLD.get()).getWidth("VERSION 1.0", 7.8F) / 2.0F, (float)(this.height / 2 - 100 + 17));

      for(Button button : this.buttons) {
         button.render(matrix4f);
         button.isHovered((double)mouseX, (double)mouseY);
      }

   }

   @Compile
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      for(Button buttonV : this.buttons) {
         if (MouseUtil.isHovered(buttonV.getX(), buttonV.getY(), buttonV.getWidth(), buttonV.getHeight(), mouseX, mouseY)) {
            buttonV.onClick();
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean isInit() {
      return this.init;
   }
}
