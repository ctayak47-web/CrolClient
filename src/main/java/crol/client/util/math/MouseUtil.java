package crol.client.util.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MouseUtil {
   public static boolean isHovered(double x, double y, double width, double height, double mouseX, double mouseY) {
      return x < mouseX && x + width > mouseX && y < mouseY && y + height > mouseY;
   }

   public static boolean isHovered2(double x, double y, double xMax, double yMax, double mouseX, double mouseY) {
      return x < mouseX && xMax > mouseX && y < mouseY && yMax > mouseY;
   }
}
