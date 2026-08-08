package crol.client.util.player;

import crol.client.event.classes.MoveOrEvent;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class StrafeMovement implements IUtil {
   private double oldSpeed;
   private double contextFriction;
   private boolean needSwap;
   private boolean needSprintState;
   private int counter;
   private int noSlowTicks;

   public double calculateSpeed(MoveOrEvent move, boolean damageBoost, boolean hasTime, boolean autoJump, float damageSpeed) {
      boolean fromGround = mc.player.isOnGround();
      boolean toGround = move.isToGround();
      boolean jump = move.getMotion().y > (double)0.0F;
      float speedAttributes = this.getAIMoveSpeed(mc.player);
      float frictionFactor = this.getFrictionFactor(mc.player, move);
      float n6 = mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && mc.player.isUsingItem() ? 0.88F : 0.91F;
      if (fromGround) {
         n6 = frictionFactor;
      }

      float n7 = 0.16277136F / (n6 * n6 * n6);
      float n8;
      if (fromGround) {
         n8 = speedAttributes * n7;
         if (jump) {
            n8 += 0.2F;
         }
      } else {
         n8 = !damageBoost || !hasTime || !autoJump && !mc.options.jumpKey.isPressed() ? 0.0255F : damageSpeed;
      }

      boolean noslow = false;
      double max2 = this.oldSpeed + (double)n8;
      double max = (double)0.0F;
      if (mc.player.isUsingItem() && !jump) {
         double n10 = this.oldSpeed + (double)n8 * (double)0.25F;
         double motionY2 = move.getMotion().y;
         if (motionY2 != (double)0.0F && Math.abs(motionY2) < 0.08) {
            n10 += 0.055;
         }

         if (max2 > (max = Math.max(0.043, n10))) {
            noslow = true;
            ++this.noSlowTicks;
         } else {
            this.noSlowTicks = Math.max(this.noSlowTicks - 1, 0);
         }
      } else {
         this.noSlowTicks = 0;
      }

      if (this.noSlowTicks > 3) {
         max2 = max - (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && mc.player.isUsingItem() ? 0.3 : 0.019);
      } else {
         max2 = Math.max(noslow ? (double)0.0F : (double)0.25F, max2) - (this.counter++ % 2 == 0 ? 0.001 : 0.002);
      }

      this.contextFriction = (double)n6;
      if (!toGround && !fromGround) {
         this.needSwap = true;
      }

      if (!fromGround && !toGround) {
         this.needSprintState = !mc.player.isSprinting();
      }

      if (toGround && fromGround) {
         this.needSprintState = false;
      }

      return max2;
   }

   public void postMove(double horizontal) {
      this.oldSpeed = horizontal * this.contextFriction;
   }

   private float getAIMoveSpeed(ClientPlayerEntity player) {
      boolean prevSprinting = player.isSprinting();
      player.setSprinting(false);
      float speed = player.getMovementSpeed() * 1.3F;
      player.setSprinting(prevSprinting);
      return speed;
   }

   private float getFrictionFactor(ClientPlayerEntity player, MoveOrEvent move) {
      BlockPos blockPos = BlockPos.ofFloored(move.getFrom().x, move.getAabbFrom().minY - (double)1.0F, move.getFrom().z);
      BlockState state = player.getWorld().getBlockState(blockPos);
      return state.getBlock().getSlipperiness() * 0.91F;
   }

   public void setOldSpeed(double oldSpeed) {
      this.oldSpeed = oldSpeed;
   }

   public void setContextFriction(double contextFriction) {
      this.contextFriction = contextFriction;
   }

   public void setNeedSwap(boolean needSwap) {
      this.needSwap = needSwap;
   }

   public void setNeedSprintState(boolean needSprintState) {
      this.needSprintState = needSprintState;
   }

   public void setCounter(int counter) {
      this.counter = counter;
   }

   public void setNoSlowTicks(int noSlowTicks) {
      this.noSlowTicks = noSlowTicks;
   }

   public double getOldSpeed() {
      return this.oldSpeed;
   }

   public double getContextFriction() {
      return this.contextFriction;
   }

   public boolean isNeedSwap() {
      return this.needSwap;
   }

   public boolean isNeedSprintState() {
      return this.needSprintState;
   }

   public int getCounter() {
      return this.counter;
   }

   public int getNoSlowTicks() {
      return this.noSlowTicks;
   }
}
