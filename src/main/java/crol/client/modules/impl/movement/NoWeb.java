package crol.client.modules.impl.movement;

import crol.client.CrolClient;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.mixins.other.IVec3dMixin;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class NoWeb extends Module implements ITickable, IUtil {
   public NoWeb() {
      super(new ModuleInfo("NoWeb", Category.MOVEMENT, "Не дает зайти игроку в паутину"));
   }

   @Compile
   public void onTick(TickEvent event) {
      if (this.isPlayerInBlock(Blocks.COBWEB)) {
         double[] speed = this.calculateDirection(0.35);
         mc.player.addVelocity(speed[0], (double)0.0F, speed[1]);
         ((IVec3dMixin)mc.player.getVelocity()).setY(mc.options.jumpKey.isPressed() ? 0.65F : (mc.options.sneakKey.isPressed() ? -0.65F : 0.0F));
      }

   }

   public boolean isPlayerInBlock(Block block) {
      return this.isBoxInBlock(mc.player.getBoundingBox().expand(-0.001), block);
   }

   public boolean isBoxInBlock(Box box, Block block) {
      return this.isBox(box, (pos) -> mc.world.getBlockState(pos).getBlock().equals(block));
   }

   public boolean isBox(Box box, Predicate<BlockPos> pos) {
      return BlockPos.stream(box).anyMatch(pos);
   }

   public double[] calculateDirection(double distance) {
      return this.calculateDirection(mc.player.input.movementForward, mc.player.input.movementSideways, distance);
   }

   @Compile
   public double[] calculateDirection(float forward, float sideways, double distance) {
      float yaw = CrolClient.INSTANCE.getRotationManager().getYaw();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sinYaw = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cosYaw = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double xMovement = (double)forward * distance * cosYaw + (double)sideways * distance * sinYaw;
      double zMovement = (double)forward * distance * sinYaw - (double)sideways * distance * cosYaw;
      return new double[]{xMovement, zMovement};
   }
}
