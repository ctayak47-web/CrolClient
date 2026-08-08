package crol.client.modules.impl.combat;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AutoArmor extends Module implements ITickable, IUtil {
   public AutoArmor() {
      super(new ModuleInfo("AutoArmor", Category.COMBAT, "NoDesc"));
   }

   public void onTick(TickEvent event) {
   }
}
