package crol.client.modules.impl.movement;

import crol.client.event.classes.RotationEvent;
import crol.client.event.interfaces.IRotateable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class GrimGlide extends Module implements IUtil, IRotateable {
   private TimerUtil ticks = new TimerUtil();
   int ticksTwo = 0;

   public GrimGlide() {
      super(new ModuleInfo("GrimGlide", Category.MOVEMENT, "NoDesc"));
   }

   public void onRotate(RotationEvent rotationEvent) {
      if (mc.player != null && mc.world != null && mc.player.isGliding()) {
         ++this.ticksTwo;
         Vec3d pos = mc.player.getPos();
         float yaw = mc.player.getYaw();
         double forward = 0.087;
         double motion = Math.hypot(mc.player.prevX - mc.player.getX(), mc.player.prevZ - mc.player.getZ()) * (double)20.0F;
         float valuePidor = 48.0F;
         if (motion >= (double)valuePidor) {
            forward = (double)0.0F;
            motion = (double)0.0F;
         }

         double dx = -Math.sin(Math.toRadians((double)yaw)) * forward;
         double dz = Math.cos(Math.toRadians((double)yaw)) * forward;
         mc.player.setVelocity(dx * (double)ThreadLocalRandom.current().nextFloat(1.1F, 1.21F), mc.player.getVelocity().y - 0.02, dz * (double)ThreadLocalRandom.current().nextFloat(1.1F, 1.21F));
         if (this.ticks.isReached(50L)) {
            mc.player.updatePosition(pos.x + dx, pos.y, pos.z + dz);
            this.ticks.reset();
         }

         mc.player.setVelocity(dx * (double)ThreadLocalRandom.current().nextFloat(1.1F, 1.21F), mc.player.getVelocity().y + 0.016, dz * (double)ThreadLocalRandom.current().nextFloat(1.1F, 1.21F));
      }
   }
}
