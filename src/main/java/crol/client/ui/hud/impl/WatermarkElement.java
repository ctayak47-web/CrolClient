package crol.client.ui.hud.impl;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.ui.draggable.impl.WatermarkDraggable;
import crol.client.ui.hud.HudElement;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBlur;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import ru_crol.crol_meta.profile.Profile;

@Environment(EnvType.CLIENT)
public class WatermarkElement extends HudElement {
   public WatermarkElement() {
      super(new WatermarkDraggable(), "Watermark");
   }

   public void render(DrawContext drawContext) {
      String username = Profile.getUsername();
      String fps = MinecraftClient.getInstance().getCurrentFps() + "fps";
      String time = "15:00";
      String uid = String.valueOf(Profile.getUid());
      float size = 7.5F;
      float width = 131.25F + ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(fps, size) + ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(username, size) + ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(time, size) + ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(uid, size);
      this.draggable.setHeight(26);
      this.draggable.setWeight((int)width);
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      Color clientColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
      BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(width, 26.0F)).radius(new QuadRadiusState(8.0F)).blurRadius(12.0F).smoothness(1.0F).color(new QuadColorState(Color.white)).build();
      blur.render(matrix, (float)this.draggable.x, (float)this.draggable.y);
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, 26.0F)).color(new QuadColorState(new Color(2048202266, true))).radius(new QuadRadiusState(8.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)this.draggable.x, (float)this.draggable.y);
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(19.0F, 18.0F)).color(new QuadColorState(clientColor)).radius(new QuadRadiusState(5.0F, 5.0F, 2.0F, 2.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)(this.draggable.x + 4), (float)(this.draggable.y + 4));
      BuiltText themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("A").color(Color.WHITE).size(10.0F).thickness(0.05F).build();
      themeText.render(matrix, (float)(this.draggable.x + 8), (float)(this.draggable.y + 7));
      float widthName = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(username, size) + 25.0F;
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(widthName, 18.0F)).color(new QuadColorState(new Color(-1206577638, true))).radius(new QuadRadiusState(3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)(this.draggable.x + 25), (float)(this.draggable.y + 4));
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("H").color(clientColor).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (double)(this.draggable.x + 30), (double)this.draggable.y + (double)8.5F);
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(username).color(Color.WHITE).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (float)(this.draggable.x + 42), (float)(this.draggable.y + 8));
      float widthFps = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(fps, size) + 23.5F;
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(widthFps, 18.0F)).color(new QuadColorState(new Color(-1206577638, true))).radius(new QuadRadiusState(3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)(this.draggable.x + 27) + widthName, (float)(this.draggable.y + 4));
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("I").color(clientColor).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (double)((float)(this.draggable.x + 32) + widthName), (double)this.draggable.y + (double)8.5F);
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("" + MinecraftClient.getInstance().getCurrentFps()).color(Color.WHITE).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (float)(this.draggable.x + 44) + widthName, (float)(this.draggable.y + 8));
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("fps").color(clientColor).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (double)this.draggable.x + (double)44.5F + (double)widthName + (double)((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth("" + MinecraftClient.getInstance().getCurrentFps(), size), (double)(this.draggable.y + 8));
      float widthTime = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(time, size) + 23.5F;
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(widthTime, 18.0F)).color(new QuadColorState(new Color(-1206577638, true))).radius(new QuadRadiusState(3.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)(this.draggable.x + 29) + widthName + widthFps, (float)(this.draggable.y + 4));
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("J").color(clientColor).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (double)((float)(this.draggable.x + 34) + widthName + widthFps), (double)this.draggable.y + (double)8.5F);
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(time).color(Color.WHITE).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (float)(this.draggable.x + 46) + widthName + widthFps, (float)(this.draggable.y + 8));
      float widthUid = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(uid, size) + 24.5F;
      rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(widthUid, 18.0F)).color(new QuadColorState(new Color(-1206577638, true))).radius(new QuadRadiusState(3.0F, 3.0F, 5.0F, 5.0F)).smoothness(1.15F).build();
      rectangle.render(matrix, (float)(this.draggable.x + 31) + widthName + widthFps + widthTime, (float)(this.draggable.y + 4));
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.WILD.get()).text("K").color(clientColor).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (double)((float)(this.draggable.x + 36) + widthName + widthFps + widthTime), (double)this.draggable.y + (double)8.5F);
      themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(uid).color(Color.WHITE).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix, (float)(this.draggable.x + 48) + widthName + widthFps + widthTime, (float)(this.draggable.y + 8));
   }
}
