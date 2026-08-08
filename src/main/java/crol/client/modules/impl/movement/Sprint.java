package crol.client.modules.impl.movement;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import crol.client.util.player.MovementUtil;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class Sprint extends Module implements ITickable, IUtil {
   private final TimerUtil timerUtil = new TimerUtil();
   private boolean canSprint = true;

   public Sprint() {
      super(new ModuleInfo("Sprint", Category.MOVEMENT, "Активирует режим бега"));
   }

   @Compile
   public void onTick(TickEvent event) {
      if (MovementUtil.isMove() && this.timerUtil.isReached((long)(45 + ThreadLocalRandom.current().nextInt(25))) && this.canSprint && mc.currentScreen == null) {
         mc.options.sprintKey.setPressed(!mc.player.isSprinting() && mc.player.input.movementForward > 0.0F && mc.player.getHungerManager().getFoodLevel() > 6);
         this.timerUtil.reset();
      }

   }

   public void setCanSprint(boolean canSprint) {
      this.canSprint = canSprint;
   }

   public TimerUtil getTimerUtil() {
      return this.timerUtil;
   }

   public boolean isCanSprint() {
      return this.canSprint;
   }
}
