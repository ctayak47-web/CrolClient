package crol.client.modules.impl.util;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NoRender extends Module {
   public final MultiBoxSetting options = ((MultiBoxSetting)(new MultiBoxSetting()).name("Options")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Fire")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Water")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Wall")).value(false), ((BooleanSetting)(new BooleanSetting()).name("NoHurtCum")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Nausea")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Darkness")).value(false));

   public NoRender() {
      super(new ModuleInfo("NoRender", Category.UTIL, "Убирает отрисовку выбранных объектов"));
      this.addSetting(new Setting[]{this.options});
   }
}
