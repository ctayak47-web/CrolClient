package crol.client.modules.impl.combat;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Items;

@Environment(EnvType.CLIENT)
public class AutoApple extends Module implements ITickable, IUtil {
   private final FloatSetting hp = ((FloatSetting)(new FloatSetting()).name("Hp")).value(15.0F).minValue(5.0F).maxValue(20.0F).incriment(0.5F);
   private boolean isEating;

   public AutoApple() {
      super(new ModuleInfo("AutoApple", Category.COMBAT, "Съедает золотое яблоко при здоровье ниже указанного"));
      this.addSetting(new Setting[]{this.hp});
   }

   public void onTick(TickEvent event) {
      this.eatGapple();
   }

   private void eatGapple() {
      if (this.conditionToEat() && this.hasGappleInHand()) {
         this.startEating();
      } else if (this.isEating) {
         this.stopEating();
      }

   }

   private void startEating() {
      if (mc.currentScreen != null) {
         mc.currentScreen.setFocused(true);
      }

      if (!mc.options.useKey.isPressed()) {
         mc.options.useKey.setPressed(true);
         this.isEating = true;
      }

   }

   private void stopEating() {
      mc.options.useKey.setPressed(false);
      this.isEating = false;
   }

   private boolean isHealthLow(float health) {
      return health <= this.hp.getValue();
   }

   private boolean conditionToEat() {
      return this.isHealthLow(mc.player.getHealth());
   }

   private boolean hasGappleInHand() {
      return mc.player.getMainHandStack().getItem() == Items.GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE;
   }
}
