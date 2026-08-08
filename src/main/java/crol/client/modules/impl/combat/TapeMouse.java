package crol.client.modules.impl.combat;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.mixins.other.IMinecraftClientMixin;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TapeMouse extends Module implements ITickable, IUtil {
   public TapeMouse() {
      super(new ModuleInfo("TapeMouse", Category.COMBAT, "Бьет с учетом кулдауна оружия"));
   }

   public void onTick(TickEvent event) {
      if (mc.player.getAttackCooldownProgress(2.0F) == 1.0F) {
         ((IMinecraftClientMixin)mc).mouseClick();
      }

   }
}
