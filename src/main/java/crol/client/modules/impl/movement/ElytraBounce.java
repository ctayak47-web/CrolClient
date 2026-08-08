package crol.client.modules.impl.movement;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;

@Environment(EnvType.CLIENT)
public class ElytraBounce extends Module implements ITickable, IUtil {
   private final FloatSetting fallDistance = ((FloatSetting)(new FloatSetting()).name("Fall Value")).minValue(1.0F).maxValue(16.0F).incriment(1.0F).value(10.0F);
   private final TimerUtil timerUtil = new TimerUtil();
   private boolean value = false;

   public ElytraBounce() {
      super(new ModuleInfo("ElytraBounce", Category.MOVEMENT, "Делает распрыжку на элитре"));
      this.addSetting(new Setting[]{this.fallDistance});
   }

   public void onTick(TickEvent event) {
      if (mc.player.isOnGround() && mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         this.value = true;
         mc.options.jumpKey.setPressed(this.value);
      }

      if (!mc.player.isOnGround() && mc.player.getMovement().y > (double)(this.fallDistance.getValue() / 100.0F) && this.timerUtil.isReached(5L) && mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         mc.options.jumpKey.setPressed(this.value);
         this.value = !this.value;
         this.timerUtil.reset();
      }

   }
}
