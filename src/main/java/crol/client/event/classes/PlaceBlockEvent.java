package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class PlaceBlockEvent extends CancellableEvent {
   private BlockPos blockPos;
   private Block block;

   public PlaceBlockEvent(BlockPos blockPos, Block block) {
      this.blockPos = blockPos;
      this.block = block;
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public Block getBlock() {
      return this.block;
   }

   public void setBlockPos(BlockPos blockPos) {
      this.blockPos = blockPos;
   }

   public void setBlock(Block block) {
      this.block = block;
   }
}
