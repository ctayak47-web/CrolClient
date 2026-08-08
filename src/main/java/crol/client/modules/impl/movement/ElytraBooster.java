package crol.client.modules.impl.movement;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ElytraBooster extends Module {
   public final FloatSetting boostValue = ((FloatSetting)(new FloatSetting()).name("Value")).value(1.5F).minValue(1.5F).maxValue(5.0F).incriment(0.01F);

   public ElytraBooster() {
      super(new ModuleInfo("ElytraBooster", Category.MOVEMENT, "Ускоряет полет игрока на элитре"));
      this.addSetting(new Setting[]{this.boostValue});
   }
}
