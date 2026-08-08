package crol.client.modules.impl.player;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.impl.StringSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NameProtect extends Module implements IUtil {
   private final StringSetting name = ((StringSetting)(new StringSetting()).name("Name")).value("Crol");

   public NameProtect() {
      super(new ModuleInfo("NameProtect", Category.PLAYER, "Скрывает реальный ник игрока"));
   }

   public String replace(String text) {
      String out = text;
      if (mc.player != null && this.isEnabled()) {
         out = text.replaceAll(mc.player.getNameForScoreboard(), this.name.getValue());
      }

      return out;
   }
}
