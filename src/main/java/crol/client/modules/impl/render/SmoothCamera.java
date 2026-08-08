package crol.client.modules.impl.render;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SmoothCamera extends Module {
   private final FloatSetting delay = ((FloatSetting)(new FloatSetting()).name("Delay")).minValue(5.0F).maxValue(50.0F).incriment(0.5F).value(5.0F);

   public SmoothCamera() {
      super(new ModuleInfo("SmoothCamera", Category.RENDER, "Добавляет камере от первого лица плавность движения"));
      this.addSetting(new Setting[]{this.delay});
   }

   public FloatSetting getDelay() {
      return this.delay;
   }
}
