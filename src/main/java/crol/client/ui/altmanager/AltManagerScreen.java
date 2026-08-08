package crol.client.ui.altmanager;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.mixins.other.IMinecraftClientMixin;
import crol.client.ui.altmanager.impl.CreateAccoutButton;
import crol.client.ui.altmanager.impl.GenerateAccoutButton;
import crol.client.ui.altmanager.impl.NickNameElement;
import crol.client.ui.altmanager.impl.StringElement;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.math.MouseUtil;
import crol.client.util.render.AnimationUtil;
import crol.client.util.render.Scissor;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBorder;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import crol.client.util.render.renderers.impl.BuiltTexture;
import java.awt.Color;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class AltManagerScreen extends Screen {
   private final Screen parent;
   private final NickNameElement nickNameElement;
   private final StringElement nameElement;
   private final StringElement tagElement;
   public int preScroll;
   public int mainScroll;
   private int click;
   private int key;
   private String type;
   private final GenerateAccoutButton generateAccoutButton;
   private final CreateAccoutButton createAccoutButton;
   private final Animation animation;

   public AltManagerScreen(Screen parent) {
      super(Text.of(""));
      this.nameElement = new StringElement(Type.NAME);
      this.tagElement = new StringElement(Type.TAG);
      this.click = -1;
      this.type = "";
      this.generateAccoutButton = new GenerateAccoutButton("", () -> {
         String name = CrolClient.INSTANCE.getNameGen().generate();
         NickName nickName = new NickName(name, this.tagElement.getValue());
         this.tagElement.setValue("");
         this.nameElement.setValue("");
         CrolClient.INSTANCE.getNickNameManager().addNickname(nickName);
         IMinecraftClientMixin instance = (IMinecraftClientMixin)MinecraftClient.getInstance();
         Session session = MinecraftClient.getInstance().getSession();
         instance.setSession(new Session(name, UUID.randomUUID(), session.getAccessToken(), session.getXuid(), session.getClientId(), AccountType.LEGACY));
         this.resetSelect();
      });
      this.createAccoutButton = new CreateAccoutButton("Create", () -> {
         NickName nickName = new NickName(this.nameElement.getValue(), this.tagElement.getValue());
         this.tagElement.setValue("");
         this.nameElement.setValue("");
         CrolClient.INSTANCE.getNickNameManager().addNickname(nickName);
         IMinecraftClientMixin instance = (IMinecraftClientMixin)MinecraftClient.getInstance();
         Session session = MinecraftClient.getInstance().getSession();
         instance.setSession(new Session(nickName.getNickname(), UUID.randomUUID(), session.getAccessToken(), session.getXuid(), session.getClientId(), AccountType.LEGACY));
         this.resetSelect();
      });
      this.parent = parent;
      this.nickNameElement = new NickNameElement();
      this.animation = new EaseBackIn(400, (double)1.0F, 0.1F, Direction.FORWARDS);
   }

   @Compile
   protected void init() {
      this.animation.reset();
      this.animation.setDirection(Direction.FORWARDS);
      double centerX = (double)(this.width / 2 - 247);
      this.createAccoutButton.updatePos(centerX + (double)10.0F, (double)(this.height / 2 + 25 + 18));
      this.generateAccoutButton.updatePos(centerX + (double)10.0F + (double)122.0F + (double)7.0F, (double)(this.height / 2 + 25 + 18));
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.initStrings(this.key, this.type);
      Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
      AnimationUtil.sizeAnimation(context, (double)(this.width / 2), (double)(this.height / 2), (double)1.35F - this.animation.getOutput() * 0.35);
      ((BuiltRectangle)Builder.rectangle().size(new SizeState((float)this.width, (float)this.height)).color(new QuadColorState(new Color(-15395300, true))).radius(new QuadRadiusState(0.0F)).smoothness(1.15F).build()).render(matrix4f, 0.0F, 0.0F);
      AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("crol", "images/mainmenu/background.png"));
      ((BuiltTexture)Builder.texture().size(new SizeState((float)this.width, (float)this.height)).radius(new QuadRadiusState(0.0F)).texture(0.0F, 0.0F, 1.0F, 1.0F, abstractTexture).color(new QuadColorState(ColorUtil.setAlpha(0.45, Color.WHITE))).build()).render(matrix4f, 0.0F, 0.0F);
      ((BuiltTexture)Builder.texture().size(new SizeState(200.0F, 200.0F)).radius(new QuadRadiusState(0.0F)).texture(0.0F, 0.0F, 1.0F, 1.0F, MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("crol", "images/mainmenu/logo.png"))).color(new QuadColorState(Color.WHITE)).build()).render(matrix4f, (float)(this.width / 2 - 100), (float)(this.height / 2 - 270));
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("WILD CLIENT").color(new Color(1295156479, true)).size(8.0F).thickness(0.05F).build()).render(matrix4f, (float)(this.width / 2) - ((MsdfFont)FontManager.BOLD.get()).getWidth("WILD CLIENT", 8.0F) / 2.0F, (float)(this.height / 2 - 160 + 27));
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("VERSION 1.0").color(new Color(775062783, true)).size(7.8F).thickness(0.05F).build()).render(matrix4f, (float)(this.width / 2) - ((MsdfFont)FontManager.BOLD.get()).getWidth("VERSION 1.0", 7.8F) / 2.0F, (float)(this.height / 2 - 160 + 37));
      double centerX = (double)(this.width / 2 - 247);
      ((BuiltRectangle)Builder.rectangle().size(new SizeState(170.0F, 110.0F)).color(new QuadColorState(new Color(-15000539, true))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build()).render(matrix4f, centerX, (double)(this.height / 2 - 35));
      ((BuiltBorder)Builder.border().size(new SizeState(170.0F, 110.0F)).color(new QuadColorState(new Color(2040108))).radius(new QuadRadiusState(5.0F)).thickness(0.01F).smoothness(0.7F, 0.7F).build()).render(matrix4f, centerX, (double)(this.height / 2 - 35));
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.MAINMENU.get()).text("F").color(new Color(-11710630, true)).size(7.0F).thickness(0.05F).build()).render(matrix4f, centerX + (double)13.0F, (double)(this.height / 2) - (double)25.5F);
      ((BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTREGULAR.get()).text("AltManager").color(new Color(-11710630, true)).size(7.0F).thickness(0.05F).build()).render(matrix4f, centerX + (double)23.5F, (double)(this.height / 2) - (double)26.5F);
      this.nameElement.button(centerX + (double)10.0F, (double)(this.height / 2 - 30 + 20), (double)150.0F, (double)20.0F, (double)mouseX, (double)mouseY, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, context, this.click);
      this.tagElement.button(centerX + (double)10.0F, (double)(this.height / 2 - 5 + 20), (double)150.0F, (double)20.0F, (double)mouseX, (double)mouseY, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, context, this.click);
      this.generateAccoutButton.render(matrix4f);
      this.createAccoutButton.render(matrix4f);
      ((BuiltRectangle)Builder.rectangle().size(new SizeState(335.0F, 210.0F)).color(new QuadColorState(new Color(-15000539, true))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build()).render(matrix4f, centerX + (double)190.0F, (double)(this.height / 2 - 85));
      ((BuiltBorder)Builder.border().size(new SizeState(335.0F, 210.0F)).color(new QuadColorState(new Color(2040108))).radius(new QuadRadiusState(5.0F)).thickness(0.01F).smoothness(0.6F, 0.6F).build()).render(matrix4f, centerX + (double)190.0F, (double)(this.height / 2 - 85));
      Scissor.StartScissor((float)((double)((float)(centerX + (double)190.0F)) - ((double)50.0F - this.animation.getOutput() * (double)50.0F)), (float)((double)((float)(this.height / 2 - 85)) - ((double)50.0F - this.animation.getOutput() * (double)50.0F)), (float)((double)335.0F * ((double)1.35F - this.animation.getOutput() * 0.35)), (float)((double)210.0F * ((double)1.35F - this.animation.getOutput() * 0.35)));
      double offsetX = (double)0.0F;
      double offsetY = (double)0.0F;

      try {
         for(NickName nickName : CrolClient.INSTANCE.getNickNameManager().getNickNames()) {
            this.nickNameElement.setNickName(nickName);
            this.nickNameElement.button(centerX + (double)200.0F + offsetX, (double)(this.height / 2 - 75) + offsetY + (double)this.mainScroll, (double)155.0F, (double)35.0F, (double)mouseX, (double)mouseY, centerX + (double)190.0F, (double)(this.height / 2 - 85), centerX + (double)190.0F + (double)335.0F, (double)(this.height / 2 - 85 + 210), context, this.click);
            offsetX += (double)160.0F;
            if (offsetX > (double)200.0F) {
               offsetX = (double)0.0F;
               offsetY += (double)39.5F;
            }
         }
      } catch (Exception var15) {
      }

      Scissor.stopScissor();
      if ((double)this.preScroll < -offsetY) {
         this.preScroll = (int)MathUtil.fast((float)this.preScroll, (float)(-offsetY), 1000.0F);
      } else if (this.preScroll > 0) {
         this.preScroll = (int)MathUtil.fast((float)this.preScroll, 0.0F, 1000.0F);
      }

      this.mainScroll = (int)MathUtil.fast((float)this.mainScroll, (float)this.preScroll, 15.0F);
      this.key = 0;
      this.type = "";
      this.click = -1;
   }

   @Compile
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.click = button;
      if (MouseUtil.isHovered(this.generateAccoutButton.getX(), this.generateAccoutButton.getY(), this.generateAccoutButton.getWidth(), this.generateAccoutButton.getHeight(), mouseX, mouseY)) {
         this.generateAccoutButton.onClick();
      }

      if (MouseUtil.isHovered(this.createAccoutButton.getX(), this.createAccoutButton.getY(), this.createAccoutButton.getWidth(), this.createAccoutButton.getHeight(), mouseX, mouseY)) {
         this.createAccoutButton.onClick();
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   private void initStrings(int key, String type) {
      this.nameElement.setKey(key);
      this.tagElement.setKey(key);
      this.nameElement.setType(type);
      this.tagElement.setType(type);
   }

   @Compile
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.key = keyCode;
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Compile
   public boolean charTyped(char chr, int modifiers) {
      this.type = String.valueOf(chr);
      return super.charTyped(chr, modifiers);
   }

   @Compile
   private void resetSelect() {
      this.nameElement.resetSelect();
      this.tagElement.resetSelect();
   }

   public StringElement getNameElement() {
      return this.nameElement;
   }

   public StringElement getTagElement() {
      return this.tagElement;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.preScroll += (int)(verticalAmount * (double)30.0F);
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }
}
