package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class CameraPositionEvent extends CancellableEvent {
   Vec3d pos;

   public CameraPositionEvent(Vec3d pos) {
      this.pos = pos;
   }

   public Vec3d getPos() {
      return this.pos;
   }

   public void setPos(Vec3d pos) {
      this.pos = pos;
   }
}
