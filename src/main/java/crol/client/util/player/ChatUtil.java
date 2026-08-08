package crol.client.util.player;

import crol.client.CrolClient;
import crol.client.util.IUtil;
import crol.client.util.color.ColorUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class ChatUtil implements IUtil {
   @Compile
   public static void addMessage(String message) {
      mc.player.sendMessage(gradient("Crol >> ", CrolClient.INSTANCE.getThemeManager().getTheme().color().darker().darker().getRGB(), CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB()).copy().append(message), false);
   }

   @Compile
   public static Text gradient(String message, int firstColor, int endColor) {
      MutableText result = Text.empty();

      for(int i = 0; i < message.length(); ++i) {
         float t = (float)i / (float)(message.length() - 1);
         int interpolatedColor = ColorUtil.interpolateColor(firstColor, endColor, t);
         Style style = Style.EMPTY.withColor(interpolatedColor);
         Text letter = Text.literal(String.valueOf(message.charAt(i))).copy().setStyle(style);
         result.append(letter);
      }

      return result;
   }
}
