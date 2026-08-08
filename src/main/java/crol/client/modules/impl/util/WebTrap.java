package crol.client.modules.impl.util;

import crol.client.CrolClient;
import crol.client.event.classes.RotationEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IRotateable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class WebTrap extends Module implements ITickable, IRotateable, IUtil, IDisableable {
   private long lastPlaceTime = 0L;
   private final long placeDelay = 250L;
   public Vec2f server = null;
   private BlockPos targetPos = null;
   private Entity targetEntity = null;
   private boolean rotationSet = false;

   public WebTrap() {
      super(new ModuleInfo("WebTrap", Category.UTIL, "Ставит паутину под противника, останавливая его"));
   }

   public void onDisable() {
      this.reset();
   }

   public void onRotate(RotationEvent e) {
      if (this.check()) {
         e.setYaw(this.server.x);
         e.setPitch(this.server.y);
         ClientPlayerEntity player = mc.player;
         if (player != null) {
            player.setHeadYaw(this.server.x);
            player.bodyYaw = this.server.x;
            CrolClient.INSTANCE.setBodyPitch(this.server.y);
         }

         this.rotationSet = true;
      }

   }

   public void onTick(TickEvent event) {
      ClientPlayerEntity player = mc.player;
      World world = mc.world;
      if (player != null && world != null) {
         if (!this.hasCobweb()) {
            this.reset();
         } else {
            if (this.targetEntity != null && (!this.targetEntity.isAlive() || this.targetEntity.getPos().squaredDistanceTo(player.getPos()) > (double)25.0F || CrolClient.INSTANCE.getFriendManager().isFriend(this.targetEntity.getName().getString()))) {
               this.reset();
            }

            Vec3d playerPos = player.getPos();
            double radius = (double)4.0F;
            Box searchBox = new Box(playerPos.x - radius, playerPos.y - radius, playerPos.z - radius, playerPos.x + radius, playerPos.y + radius, playerPos.z + radius);
            List<PlayerEntity> candidates = world.getEntitiesByClass(PlayerEntity.class, searchBox, (e) -> e.isAlive() && e != player && e.isOnGround() && Math.abs(e.getX() - e.prevX) < 0.01 && Math.abs(e.getZ() - e.prevZ) < 0.01 && CrolClient.INSTANCE.getFriendManager().isFriend(e.getName().getString()));
            this.targetEntity = (Entity)candidates.stream().min((e1, e2) -> Double.compare(e1.getPos().squaredDistanceTo(playerPos), e2.getPos().squaredDistanceTo(playerPos))).orElse((PlayerEntity) null);
            if (this.targetEntity == null) {
               this.reset();
            } else {
               Vec3d entityPos = this.targetEntity.getPos();
               boolean headInWeb = false;
               boolean feetInWeb = false;

               label57:
               for(double x = -0.31; x <= 0.31; x += 0.31) {
                  for(double z = -0.31; z <= 0.31; z += 0.31) {
                     BlockPos headPos = BlockPos.ofFloored(entityPos.x + x, entityPos.y + (double)this.targetEntity.getStandingEyeHeight(), entityPos.z + z);
                     BlockPos feetPos = BlockPos.ofFloored(entityPos.x + x, entityPos.y, entityPos.z + z);
                     if (world.getBlockState(headPos).getBlock() == Blocks.COBWEB) {
                        headInWeb = true;
                     }

                     if (world.getBlockState(feetPos).getBlock() == Blocks.COBWEB) {
                        feetInWeb = true;
                     }

                     if (headInWeb && feetInWeb) {
                        break label57;
                     }
                  }
               }

               this.targetPos = this.getTargetPosition(this.targetEntity, feetInWeb, headInWeb);
               if (this.targetPos == null) {
                  this.reset();
               } else {
                  this.updateRotation();
                  if (this.rotationSet) {
                     long now = System.currentTimeMillis();
                     if (now - this.lastPlaceTime >= 250L) {
                        this.placeCobweb(player);
                        this.lastPlaceTime = now;
                     }
                  }

               }
            }
         }
      } else {
         this.reset();
      }
   }

   private boolean check() {
      return this.server != null && this.targetPos != null && this.targetEntity != null && this.targetEntity.isAlive();
   }

   private void reset() {
      this.targetPos = null;
      this.server = null;
      this.targetEntity = null;
      this.rotationSet = false;
   }

   private boolean isCobwebItem(Item item) {
      return item == Items.COBWEB;
   }

   private int getCobwebHotbarSlot() {
      PlayerInventory inv = mc.player.getInventory();

      for(int i = 0; i < 9; ++i) {
         if (this.isCobwebItem(inv.getStack(i).getItem())) {
            return i;
         }
      }

      return -1;
   }

   private int getCobwebInventoryIndex() {
      PlayerInventory inv = mc.player.getInventory();

      for(int i = 9; i < 36; ++i) {
         if (this.isCobwebItem(inv.getStack(i).getItem())) {
            return i;
         }
      }

      return -1;
   }

   private boolean hasCobweb() {
      return this.getCobwebHotbarSlot() != -1 || this.getCobwebInventoryIndex() != -1;
   }

   private BlockPos getTargetPosition(Entity entity, boolean feetInWeb, boolean headInWeb) {
      Vec3d pos = entity.getPos();
      BlockPos feetPos = BlockPos.ofFloored(pos);
      BlockPos headPos = feetPos.up();
      World world = mc.world;
      if (feetInWeb && !headInWeb) {
         if (world.getBlockState(headPos).isAir() && world.getBlockState(feetPos).isSolidBlock(world, feetPos)) {
            return headPos;
         }
      } else if (!feetInWeb && !headInWeb) {
         BlockPos belowFeet = feetPos.down();
         if (world.getBlockState(feetPos).isAir() && world.getBlockState(belowFeet).isSolidBlock(world, belowFeet)) {
            return feetPos;
         }
      }

      return null;
   }

   private void updateRotation() {
      if (this.targetPos != null) {
         Vec3d targetVec = new Vec3d((double)this.targetPos.getX() + (double)0.5F, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + (double)0.5F);
         Vec3d eye = mc.player.getCameraPosVec(1.0F);
         Vec3d vec = targetVec.subtract(eye);
         float yawToTarget = MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0F);
         float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(vec.y, Math.sqrt(vec.x * vec.x + vec.z * vec.z))));
         if (this.server == null) {
            this.server = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
         }

         float pitch = MathHelper.clamp(pitchToTarget, -89.0F, 89.0F);
         float gcd = this.getGCDValue();
         float yaw = yawToTarget - (yawToTarget - this.server.x) % gcd;
         pitch -= (pitch - this.server.y) % gcd;
         this.server = new Vec2f(yaw, pitch);
      }
   }

   private float getGCDValue() {
      float sensitivity = (float)((Double)mc.options.getMouseSensitivity().getValue() * (double)0.6F + (double)0.2F);
      float gcd = sensitivity * sensitivity * sensitivity * 8.0F;
      return gcd * 0.15F;
   }

   private void placeCobweb(ClientPlayerEntity player) {
      PlayerInventory inv = player.getInventory();
      int originalSlot = inv.selectedSlot;
      boolean swapped = false;
      int swapFromIndex = -1;
      int swapToSlot = -1;
      int hotbarSlot = this.getCobwebHotbarSlot();
      if (hotbarSlot != -1) {
         if (hotbarSlot != inv.selectedSlot) {
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
            inv.selectedSlot = hotbarSlot;
         }
      } else {
         swapFromIndex = this.getCobwebInventoryIndex();
         if (swapFromIndex == -1) {
            this.reset();
            return;
         }

         swapToSlot = (inv.selectedSlot + 1) % 9;
         mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, swapFromIndex, swapToSlot, SlotActionType.SWAP, player);
         player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(swapToSlot));
         inv.selectedSlot = swapToSlot;
         swapped = true;
      }

      BlockPos belowPos = this.targetPos.down();
      BlockHitResult hitResult = new BlockHitResult(new Vec3d((double)this.targetPos.getX() + (double)0.5F, (double)this.targetPos.getY(), (double)this.targetPos.getZ() + (double)0.5F), Direction.UP, belowPos, false);
      mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
      player.swingHand(Hand.MAIN_HAND);
      player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
      inv.selectedSlot = originalSlot;
      if (swapped && swapFromIndex != -1) {
         mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, swapFromIndex, swapToSlot, SlotActionType.SWAP, player);
      }

   }
}
