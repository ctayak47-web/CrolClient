package crol.client.ui.hud.impl;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.ui.hud.HudElement;
import crol.client.ui.notify.Notify;
import crol.client.util.IUtil;
import crol.client.util.animations.Direction;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MathUtil;
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
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class NotifyElement extends HudElement {
   public void render(DrawContext drawContext) {
      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      float offset = 0.0F;
      CrolClient.INSTANCE.getNotifyManager().updateNotify();

      for(Notify notify : CrolClient.INSTANCE.getNotifyManager().getNotifies().reversed()) {
         double x = notify.getX();
         double y = notify.getY();
         Color colorWhite = ColorUtil.setAlpha(notify.getAnimation().getOutput(), Color.white);
         Color clientColor = ColorUtil.setAlpha(notify.getAnimation().getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color());
         BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(notify.getWidth(), (double)26.0F)).radius(new QuadRadiusState(8.0F)).blurRadius((float)((double)12.0F * notify.getAnimation().getOutput())).smoothness(1.0F).color(new QuadColorState(Color.white)).build();
         blur.render(matrix, x, y);
         ((BuiltRectangle)Builder.rectangle().size(new SizeState(notify.getWidth(), (double)26.0F)).color(new QuadColorState(ColorUtil.setAlpha(notify.getAnimation().getOutput(), new Color(2048202266, true)))).radius(new QuadRadiusState(8.0F)).smoothness(1.15F).build()).render(matrix, x, y);
         BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(19.0F, 18.0F)).color(new QuadColorState(clientColor)).radius(new QuadRadiusState(5.0F, 5.0F, 2.0F, 2.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, x + (double)4.0F, y + (double)4.0F);
         BuiltText textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.ICONS.get()).text("I").color(colorWhite).size(10.0F).thickness(0.05F).build();
         textRender.render(matrix, x + (double)9.5F, y + (double)7.25F);
         ((BuiltRectangle)Builder.rectangle().size(new SizeState(notify.getWidth() - (double)30.0F, (double)18.0F)).color(new QuadColorState(ColorUtil.setAlpha(notify.getAnimation().getOutput(), new Color(-1206577638, true)))).radius(new QuadRadiusState(2.0F, 2.0F, 5.0F, 5.0F)).smoothness(1.15F).build()).render(matrix, x + (double)26.0F, y + (double)4.0F);
         textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(notify.getText()).color(colorWhite).size(7.5F).thickness(0.05F).build();
         textRender.render(matrix, x + (double)31.0F, y + (double)8.0F);
         notify.setY((double)MathUtil.fast((float)notify.getY(), (float)(IUtil.mc.getWindow().getScaledHeight() / 2 + 16) + offset, 10.0F));
         if (notify.getTimerUtil().isReached(1000L) && offset / 28.0F == (float)(CrolClient.INSTANCE.getNotifyManager().getNotifies().size() - 1)) {
            notify.getAnimation().setDirection(Direction.BACKWARDS);
         }

         offset += 28.0F;
      }

   }
}
