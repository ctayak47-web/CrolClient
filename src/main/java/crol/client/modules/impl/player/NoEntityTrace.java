package crol.client.modules.impl.player;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.SwordItem;

@Environment(EnvType.CLIENT)
public class NoEntityTrace extends Module implements IUtil {
   public BooleanSetting noSword = ((BooleanSetting)(new BooleanSetting()).name("No Sword")).value(false);

   public NoEntityTrace() {
      super(new ModuleInfo("NoEntityTrace", Category.PLAYER, "Позволяет взаимодействовать с интерактивными блоками через сущностей"));
   }

   public boolean shouldIgnoreEntityTrace() {
      return this.isEnabled() && (!(mc.player.getMainHandStack().getItem() instanceof SwordItem) || !this.noSword.getValue());
   }
}
