package crol.client.modules.impl.combat;

import crol.client.event.classes.PlaceBlockEvent;
import crol.client.event.classes.RotationEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IPlaceBlockable;
import crol.client.event.interfaces.IRotateable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.util.IUtil;
import crol.client.util.aura.AuraUtil;
import crol.client.util.math.TimerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class AutoExplosion extends Module implements ITickable, IRotateable, IPlaceBlockable, IUtil, IDisableable {
   private boolean pendingPlace = false;
   private boolean placed = false;
   private BlockPos targetPos = null;
   private final TimerUtil placeTimer = new TimerUtil();
   private final TimerUtil explodeTimer = new TimerUtil();
   public final BooleanSetting breake = ((BooleanSetting)(new BooleanSetting() {
      public void onChangeState(boolean state) {
         AutoExplosion.this.placed = false;
         super.onChangeState(state);
      }
   }).name("Break")).value(false);

   public AutoExplosion() {
      super(new ModuleInfo("AutoExplosion", Category.COMBAT, "Ставит кристалл Энда на размещенный обсидиан"));
      this.addSetting(new Setting[]{this.breake});
   }

   public void onDisable() {
      this.reset();
   }

   public void onTick(TickEvent event) {
      if (mc.player != null && mc.world != null) {
         if (this.pendingPlace && this.placeTimer.isReached(100L)) {
            this.placeCrystal(this.targetPos);
            this.pendingPlace = false;
         }

      }
   }

   public void onPlace(PlaceBlockEvent event) {
      if (mc.player != null && mc.world != null) {
         if (event.getBlock() == Blocks.OBSIDIAN) {
            BlockPos pos = event.getBlockPos();
            if (mc.world.getBlockState(pos.up()).isAir()) {
               if (mc.world.getBlockState(pos.up(2)).isAir()) {
                  if (this.getSlotWithCrystal() != -1) {
                     this.targetPos = pos;
                     this.pendingPlace = true;
                     this.placeTimer.reset();
                  }
               }
            }
         }
      }
   }

   public void onRotate(RotationEvent rotationEvent) {
      if (mc.player != null && mc.world != null) {
         if (this.placed && this.breake.getValue()) {
            if (this.explodeTimer.isReached(50L)) {
               EndCrystalEntity target = this.findBestCrystal(rotationEvent.getYaw());
               if (target != null) {
                  mc.interactionManager.attackEntity(mc.player, target);
                  mc.player.swingHand(Hand.MAIN_HAND);
                  this.placed = false;
                  this.targetPos = null;
               }
            }
         }
      }
   }

   private void placeCrystal(BlockPos position) {
      if (position != null && mc.player != null && mc.world != null) {
         if (this.isValidBase(position)) {
            if (mc.world.getBlockState(position.up()).isAir()) {
               if (mc.world.getBlockState(position.up(2)).isAir()) {
                  int crystalSlot = this.getSlotWithCrystal();
                  if (crystalSlot != -1) {
                     int oldSlot = mc.player.getInventory().selectedSlot;
                     mc.player.getInventory().selectedSlot = crystalSlot;
                     Vec3d hitPos = Vec3d.ofCenter(position);
                     BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, position, false);
                     ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                     if (result.isAccepted()) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                        this.placed = true;
                        this.explodeTimer.reset();
                     }

                     mc.player.getInventory().selectedSlot = oldSlot;
                  }
               }
            }
         }
      }
   }

   private EndCrystalEntity findBestCrystal(float currentYaw) {
      EndCrystalEntity best = null;
      double bestDist = Double.MAX_VALUE;

      for(Entity entity : mc.world.getEntities()) {
         if (entity instanceof EndCrystalEntity crystal) {
            if (mc.player.canSee(crystal) && !(mc.player.distanceTo(crystal) > 6.0F) && AuraUtil.canSeeEntityAtFov(crystal, 60.0F, currentYaw)) {
               if (this.targetPos != null && crystal.getBlockPos().equals(this.targetPos.up())) {
                  return crystal;
               }

               double dist = (double)mc.player.distanceTo(crystal);
               if (dist < bestDist) {
                  bestDist = dist;
                  best = crystal;
               }
            }
         }
      }

      return best;
   }

   private boolean isValidBase(BlockPos pos) {
      Block block = mc.world.getBlockState(pos).getBlock();
      return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
   }

   private int getSlotWithCrystal() {
      for(int i = 0; i < 9; ++i) {
         if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
            return i;
         }
      }

      return -1;
   }

   private void reset() {
      this.pendingPlace = false;
      this.placed = false;
      this.targetPos = null;
   }
}
