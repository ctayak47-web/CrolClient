package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class MoveEvent extends CancellableEvent {
   private Vec3d movement;

   public MoveEvent(Vec3d movement) {
      this.movement = movement;
   }

   public Vec3d getMovement() {
      return this.movement;
   }

   public void setMovement(Vec3d movement) {
      this.movement = movement;
   }
}
