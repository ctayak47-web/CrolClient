package crol.client.modules.impl.movement;

import crol.client.event.classes.Render3DEvent;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Parkour extends Module implements IRenderable3D, IUtil {
   private final TimerUtil timerUtil = new TimerUtil();

   public Parkour() {
      super(new ModuleInfo("Parkour", Category.MOVEMENT, "Прыгает на конце блока"));
   }

   public void onRender3D(Render3DEvent event) {
      if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand((double)-0.01F, (double)0.0F, (double)-0.01F).offset((double)0.0F, -0.99, (double)0.0F)).iterator().hasNext() && this.timerUtil.every((double)150.0F)) {
         mc.player.jump();
      }

   }
}
