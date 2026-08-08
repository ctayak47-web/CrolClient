package crol.client.modules.impl.util;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientSound extends Module {
   public final FloatSetting value = ((FloatSetting)(new FloatSetting()).name("Value")).value(15.0F).minValue(1.0F).maxValue(35.0F).incriment(1.0F);

   public ClientSound() {
      super(new ModuleInfo("ClientSound", Category.UTIL, "Проигрывает звук при переключении функции"));
      this.addSetting(new Setting[]{this.value});
   }
}
