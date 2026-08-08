package crol.client.ui.altmanager.impl;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.mixins.other.IMinecraftClientMixin;
import crol.client.ui.altmanager.NickName;
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
import crol.client.util.render.renderers.impl.BuiltTexture;
import java.awt.Color;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class NickNameElement {
   private NickName nickName;

   public void button(double x, double y, double width, double height, double mouseX, double mouseY, double xStart, double yStart, double xEnd, double yEnd, DrawContext drawContext, int click) {
      Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
      ((BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(new Color(-14737108, true))).radius(new QuadRadiusState(4.0F)).smoothness(1.15F).build()).render(matrix4f, x, y);
      ((BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(new Color(2434866))).radius(new QuadRadiusState(4.0F)).thickness(0.01F).smoothness(0.7F, 0.7F).build()).render(matrix4f, x, y);
      double valAnimation = this.nickName.getAnimation().getOutput();
      if (!this.nickName.getAnimation().finished(Direction.BACKWARDS)) {
         ((BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(valAnimation, new Color(-14342350, true)))).radius(new QuadRadiusState(4.0F)).smoothness(1.15F).build()).render(matrix4f, x, y);
         ((BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(ColorUtil.setAlpha(valAnimation, new Color(3092796)))).radius(new QuadRadiusState(4.0F)).thickness(0.01F).smoothness(0.7F, 0.7F).build()).render(matrix4f, x, y);
      }

      AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.ofVanilla("textures/entity/player/wide/steve.png"));
      BuiltTexture texture = (BuiltTexture)Builder.texture().size(new SizeState(height - (double)10.0F, height - (double)10.0F)).radius(new QuadRadiusState(2.0F)).texture(0.125F, 0.125F, 0.125F, 0.125F, abstractTexture).color(new QuadColorState(Color.white)).build();
      texture.render(matrix4f, x + (double)5.0F, y + (double)5.0F);
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.nickName.getNickname()).color(new Color(12961746)).size(8.0F).thickness(0.05F).build();
      text.render(matrix4f, x + (height - (double)10.0F) + (double)10.0F, y + (double)8.0F);
      text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.nickName.getTag()).color(new Color(7040376)).size(7.0F).thickness(0.05F).build();
      text.render(matrix4f, x + (height - (double)10.0F) + (double)10.0F, y + (double)19.0F);
      double deleteButtonWidth = height - (double)15.0F;
      if (MouseUtil.isHovered(x, y, width - deleteButtonWidth, height, mouseX, mouseY) && click == 0) {
         IMinecraftClientMixin instance = (IMinecraftClientMixin)MinecraftClient.getInstance();
         Session session = MinecraftClient.getInstance().getSession();
         instance.setSession(new Session(this.nickName.getNickname(), UUID.randomUUID(), session.getAccessToken(), session.getXuid(), session.getClientId(), AccountType.LEGACY));
         CrolClient.INSTANCE.getConfigManager().saveNickNames();
      }

      this.deleteButton(x + width - (deleteButtonWidth + (double)6.0F), y + (double)8.0F, deleteButtonWidth, deleteButtonWidth, mouseX, mouseY, matrix4f, click);
      if (!this.nickName.getAnimation().finished(Direction.BACKWARDS)) {
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.nickName.getNickname()).color(ColorUtil.setAlpha(valAnimation, new Color(16777215))).size(8.0F).thickness(0.05F).build();
         text.render(matrix4f, x + (height - (double)10.0F) + (double)10.0F, y + (double)8.0F);
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.nickName.getTag()).color(ColorUtil.setAlpha(valAnimation, new Color(12961746))).size(7.0F).thickness(0.05F).build();
         text.render(matrix4f, x + (height - (double)10.0F) + (double)10.0F, y + (double)19.0F);
      }

      if (MinecraftClient.getInstance().getSession().getUsername().equals(this.nickName.getNickname())) {
         this.nickName.getAnimation().setDirection(Direction.FORWARDS);
      } else {
         this.nickName.getAnimation().setDirection(Direction.BACKWARDS);
      }

   }

   private void deleteButton(double x, double y, double width, double height, double mouseX, double mouseY, Matrix4f matrix4f, int click) {
      ((BuiltRectangle)Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(new Color(-14342350, true))).radius(new QuadRadiusState(3.0F)).smoothness(1.15F).build()).render(matrix4f, x, y);
      ((BuiltBorder)Builder.border().size(new SizeState(width, height)).color(new QuadColorState(new Color(3092796))).radius(new QuadRadiusState(3.0F)).thickness(0.01F).smoothness(0.7F, 0.7F).build()).render(matrix4f, x, y);
      BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.MAINMENU.get()).text("D").color(new Color(7040376)).size(8.0F).thickness(0.05F).build();
      text.render(matrix4f, x + width / (double)2.0F - (double)(((MsdfFont)FontManager.MAINMENU.get()).getWidth("D", 8.0F) / 2.0F) + (double)0.5F, y + (double)6.0F);
      double valAnimation = this.nickName.getAnimation().getOutput();
      if (!this.nickName.getAnimation().finished(Direction.BACKWARDS)) {
         Color color1 = ColorUtil.setAlpha(valAnimation, new Color(-49040, true));
         Color color2 = ColorUtil.setAlpha(valAnimation, new Color(-52682, true));
         ((BuiltRectangle)Builder.rectangle().size(new SizeState(width + (double)2.0F, height + (double)2.0F)).color(new QuadColorState(color1, color2, color1, color2)).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build()).render(matrix4f, x - (double)1.0F, y - (double)1.0F);
         text = (BuiltText)Builder.text().font((MsdfFont)FontManager.MAINMENU.get()).text("D").color(ColorUtil.setAlpha(valAnimation, new Color(16777215))).size(8.0F).thickness(0.05F).build();
         text.render(matrix4f, x + width / (double)2.0F - (double)(((MsdfFont)FontManager.MAINMENU.get()).getWidth("D", 8.0F) / 2.0F) + (double)0.5F, y + (double)6.0F);
      }

      if (MouseUtil.isHovered(x, y, width, height, mouseX, mouseY) && click == 0) {
         CrolClient.INSTANCE.getNickNameManager().remove(this.nickName);
      }

   }

   public void setNickName(NickName nickName) {
      this.nickName = nickName;
   }
}
