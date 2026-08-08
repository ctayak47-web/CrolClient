package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PostMoveEvent extends CancellableEvent {
   private double horizontalMove;

   public PostMoveEvent(double horizontalMove) {
      this.horizontalMove = horizontalMove;
   }

   public double getHorizontalMove() {
      return this.horizontalMove;
   }

   public void setHorizontalMove(double horizontalMove) {
      this.horizontalMove = horizontalMove;
   }
}
