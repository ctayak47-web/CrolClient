package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class BreakEvent extends CancellableEvent {
   private BlockPos blockPos;
   private Direction direction;

   public BreakEvent(BlockPos blockPos, Direction direction) {
      this.blockPos = blockPos;
      this.direction = direction;
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public Direction getDirection() {
      return this.direction;
   }
}
