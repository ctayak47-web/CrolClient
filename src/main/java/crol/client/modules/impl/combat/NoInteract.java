package crol.client.modules.impl.combat;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NoInteract extends Module {
   public NoInteract() {
      super(new ModuleInfo("NoInteract", Category.COMBAT, "Отключает взаимодействие с интерактивными блоками"));
   }
}
