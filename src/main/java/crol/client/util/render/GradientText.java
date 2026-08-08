package crol.client.util.render;

import crol.client.util.IUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class GradientText implements IUtil {
   private int[] colors = new int[2500];

   public void drawText(Matrix4f matrixStack, MsdfFont font, String text, int x, int y, float size) {
      int xOffset = 0;

      for(char c : text.toCharArray()) {
         BuiltText textRender = (BuiltText)Builder.text().font(font).text(String.valueOf(c)).color(new Color(this.colors[xOffset * 4])).size(size).thickness(0.05F).build();
         textRender.render(matrixStack, (float)(x + xOffset), (float)y);
         xOffset = (int)((float)xOffset + font.getWidth(String.valueOf(c), size) + 0.7F);
      }

   }

   public void update() {
   }

   public int[] getColors() {
      return this.colors;
   }
}
