package crol.client.modules.impl.movement;

import crol.client.CrolClient;
import crol.client.event.classes.MoveOrEvent;
import crol.client.event.classes.PostMoveEvent;
import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IMoveOrable;
import crol.client.event.interfaces.IPostMovaable;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import crol.client.util.player.StrafeMovement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class Strafe extends Module implements ITickable, IUtil, IEnableable, IPostMovaable, IReceivePacketable, IMoveOrable {
   public BooleanSetting potionBoost = ((BooleanSetting)(new BooleanSetting()).name("PotionBoost")).value(false);
   public BooleanSetting damageBoost = ((BooleanSetting)(new BooleanSetting()).name("DamageBoost")).value(false);
   private final TimerUtil timerUtil = new TimerUtil();
   private TargetStrafe targetStrafe;
   private final StrafeMovement strafeMovement = new StrafeMovement();

   public Strafe() {
      super(new ModuleInfo("Strafe", Category.MOVEMENT, "Ускоряет изменение вектора движения игрока"));
      this.addSetting(new Setting[]{this.potionBoost, this.damageBoost});
   }

   public void onEnable() {
      this.strafeMovement.setOldSpeed((double)0.0F);
      this.targetStrafe = (TargetStrafe)CrolClient.INSTANCE.getModuleManager().getByClass(TargetStrafe.class);
   }

   public void onMoveOrable(MoveOrEvent moveOrEvent) {
      if (!this.targetStrafeCheck()) {
         this.handleEventMove(moveOrEvent);
      }
   }

   public void onPostMove(PostMoveEvent postMoveEvent) {
      if (!this.targetStrafeCheck()) {
         this.handleEventPostMove(postMoveEvent);
      }
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (!this.targetStrafeCheck()) {
         this.handleEventPacket(receivePacketEvent);
      }
   }

   public void onTick(TickEvent event) {
      if (!this.targetStrafeCheck()) {
         this.handleEventAction(mc.player.isSprinting());
      }
   }

   private void handleEventAction(boolean isSprint) {
      if (this.strafes()) {
         this.handleStrafesEventAction(isSprint);
      }

      if (this.strafeMovement.isNeedSwap()) {
         this.handleNeedSwapEventAction(isSprint);
      }

   }

   private void handleEventMove(MoveOrEvent eventMove) {
      if (this.strafes()) {
         this.handleStrafesEventMove(eventMove);
      } else {
         this.strafeMovement.setOldSpeed((double)0.0F);
      }

   }

   private void handleEventPostMove(PostMoveEvent eventPostMove) {
      this.strafeMovement.postMove(eventPostMove.getHorizontalMove());
   }

   private void handleEventPacket(ReceivePacketEvent packet) {
      this.handleReceivePacketEventPacket(packet);
   }

   private void handleStrafesEventAction(boolean isSprint) {
      mc.options.sprintKey.setPressed(true);
   }

   private void handleStrafesEventMove(MoveOrEvent eventMove) {
      float damageSpeed = 1.0F;
      double speed = this.strafeMovement.calculateSpeed(eventMove, false, false, false, 1.0F);
      this.setMoveMotion(eventMove, speed);
      if (this.potionBoost.getValue()) {
         for(StatusEffectInstance effect : mc.player.getStatusEffects()) {
            String mutableText = ((StatusEffect)effect.getEffectType().value()).getTranslationKey();
            if (mutableText.equalsIgnoreCase("effect.minecraft.speed")) {
               int lvl = effect.getAmplifier() + 1;
               switch (lvl) {
                  case 0:
                     this.setMoveMotion(eventMove, speed);
                     break;
                  case 1:
                     this.setMoveMotion(eventMove, speed * 1.01);
                     break;
                  case 2:
                     this.setMoveMotion(eventMove, speed * 1.03);
                     break;
                  case 3:
                     this.setMoveMotion(eventMove, speed * 1.055);
                     break;
                  case 4:
                     this.setMoveMotion(eventMove, speed * 1.061);
               }
            }
         }

      }
   }

   private void setMoveMotion(MoveOrEvent move, double motion) {
      double forward = (double)mc.player.input.movementForward;
      double strafe = (double)mc.player.input.movementSideways;
      float yaw = mc.player.getYaw();
      if (forward == (double)0.0F && strafe == (double)0.0F) {
         move.getMotion().x = (double)0.0F;
         move.getMotion().z = (double)0.0F;
      } else {
         if (forward != (double)0.0F) {
            if (strafe > (double)0.0F) {
               yaw += (float)(forward > (double)0.0F ? -45 : 45);
            } else if (strafe < (double)0.0F) {
               yaw += (float)(forward > (double)0.0F ? 45 : -45);
            }

            strafe = (double)0.0F;
            if (forward > (double)0.0F) {
               forward = (double)1.0F;
            } else if (forward < (double)0.0F) {
               forward = (double)-1.0F;
            }
         }

         mc.player.setVelocity(forward * motion * (double)MathHelper.cos((float)Math.toRadians((double)(yaw + 90.0F))) + strafe * motion * (double)MathHelper.sin((float)Math.toRadians((double)(yaw + 90.0F))), mc.player.getVelocity().y, forward * motion * (double)MathHelper.sin((float)Math.toRadians((double)(yaw + 90.0F))) - strafe * motion * (double)MathHelper.cos((float)Math.toRadians((double)(yaw + 90.0F))));
      }

   }

   private void handleNeedSwapEventAction(boolean sprint) {
      mc.player.setSprinting(!CrolClient.INSTANCE.getRotationManager().isServerSprint());
      this.strafeMovement.setNeedSwap(false);
   }

   private void handleReceivePacketEventPacket(ReceivePacketEvent packet) {
      if (packet.getPacket() instanceof PlayerPositionLookS2CPacket) {
         this.strafeMovement.setOldSpeed((double)0.0F);
      }

   }

   public boolean strafes() {
      if (this.isInvalidPlayerState()) {
         return false;
      } else {
         BlockPos playerPosition = new BlockPos((int)mc.player.getPos().x, (int)mc.player.getPos().y, (int)mc.player.getPos().z);
         BlockPos abovePosition = playerPosition.up();
         BlockPos belowPosition = playerPosition.down();
         if (this.isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
         } else {
            return this.isPlayerInWebOrSoulSand(playerPosition) ? false : this.isPlayerAbleToStrafe();
         }
      }
   }

   private boolean isInvalidPlayerState() {
      return mc.player == null || mc.world == null || mc.player.isSneaking() || mc.player.isGliding() || mc.player.isTouchingWater() || mc.player.isInLava();
   }

   private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
      Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
      Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();
      return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
   }

   private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
      Block block = mc.world.getBlockState(playerPosition).getBlock();
      Block below = mc.world.getBlockState(playerPosition.down()).getBlock();
      boolean inWeb = block instanceof CobwebBlock;
      boolean onSoulSand = below instanceof SoulSandBlock;
      return inWeb || onSoulSand;
   }

   private boolean isPlayerAbleToStrafe() {
      return !mc.player.getAbilities().allowFlying && !mc.player.hasStatusEffect(StatusEffects.LEVITATION);
   }

   private boolean targetStrafeCheck() {
      return this.targetStrafe.isEnabled() && this.targetStrafe.getTarget() != null;
   }
}
