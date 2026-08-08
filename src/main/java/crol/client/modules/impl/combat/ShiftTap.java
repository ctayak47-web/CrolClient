package crol.client.modules.impl.combat;

import crol.client.event.classes.AttackEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IAttackable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ShiftTap extends Module implements ITickable, IAttackable, IUtil {
   long shiftTapEndTime = 0L;
   boolean isModuleControllingSneak = false;

   public ShiftTap() {
      super(new ModuleInfo("ShiftTap", Category.COMBAT, "Присаживается после удара"));
   }

   private void startShiftTap() {
      this.shiftTapEndTime = System.currentTimeMillis() + 25L;
      if (!this.isModuleControllingSneak) {
         mc.options.sneakKey.setPressed(true);
         this.isModuleControllingSneak = true;
      }

   }

   private void stopShiftTap() {
      if (this.isModuleControllingSneak) {
         mc.options.sneakKey.setPressed(false);
         this.isModuleControllingSneak = false;
      }

   }

   public void onAttack(AttackEvent event) {
      if (mc.player != null) {
         this.startShiftTap();
      }
   }

   public void onTick(TickEvent event) {
      if (mc.player != null && !mc.player.isSpectator()) {
         long currentTime = System.currentTimeMillis();
         if (this.isModuleControllingSneak && currentTime > this.shiftTapEndTime) {
            this.stopShiftTap();
         }

      } else {
         this.stopShiftTap();
      }
   }
}
