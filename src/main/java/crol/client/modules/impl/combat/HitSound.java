package crol.client.modules.impl.combat;

import crol.client.event.classes.AttackEvent;
import crol.client.event.interfaces.IAttackable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.other.SoundUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class HitSound extends Module implements IAttackable {
   private final FloatSetting value = ((FloatSetting)(new FloatSetting()).name("Value")).value(15.0F).minValue(0.0F).maxValue(30.0F).incriment(1.0F);
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("Type1").modes("Type1", "Type2", "Type3", "Type4", "Type5", "Type6", "Type7");

   public HitSound() {
      super(new ModuleInfo("HitSound", Category.COMBAT, "Проигрывает звук при ударе по противнику"));
      this.addSetting(new Setting[]{this.value, this.mode});
   }

   @Compile
   public void onAttack(AttackEvent event) {
      SoundUtil.playHitSound(this.mode.getValue().toLowerCase() + ".wav", 65.0F + this.value.getValue(), false);
   }
}
