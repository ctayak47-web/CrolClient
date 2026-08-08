package crol.client.modules.impl.movement;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.mixins.other.ILivingEntityMixin;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NoJumpDelay extends Module implements ITickable, IUtil {
   public NoJumpDelay() {
      super(new ModuleInfo("NoJumpDelay", Category.MOVEMENT, "Спаммит прыжками в низком пространстве"));
   }

   public void onTick(TickEvent event) {
      ((ILivingEntityMixin)mc.player).setJumpingCooldown(0);
   }
}
