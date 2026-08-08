package crol.client.modules.impl.movement;

import crol.client.CrolClient;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.impl.combat.Aura;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class TargetStrafe extends Module implements ITickable, IUtil, IEnableable {
   private final FloatSetting getDistance = ((FloatSetting)(new FloatSetting()).name("Distance")).value(1.0F).minValue(0.0F).maxValue(6.0F).incriment(0.05F);
   private final BooleanSetting potionBoost = ((BooleanSetting)(new BooleanSetting()).name("PotionBoost")).value(false);
   private final BooleanSetting jump = ((BooleanSetting)(new BooleanSetting()).name("Jump")).value(false);
   private float side = 1.0F;
   private LivingEntity target = null;
   private int potionBoostLvl = 0;
   private Aura aura;

   public TargetStrafe() {
      super(new ModuleInfo("TargetStrafe", Category.MOVEMENT, "Преследует противника"));
      this.addSetting(new Setting[]{this.getDistance, this.potionBoost, this.jump});
   }

   public void onTick(TickEvent event) {
      if (mc.player != null && mc.world != null) {
         if (mc.player.age >= 10) {
            LivingEntity auraTarget = this.getTarget();
            if (auraTarget != null) {
               this.target = auraTarget;
               if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed() && this.target.isAlive() && this.jump.getValue()) {
                  mc.player.jump();
               }

               if (this.target.isAlive() && !(this.target.getHealth() <= 0.0F)) {
                  if (mc.player.horizontalCollision) {
                     this.side *= -1.0F;
                  }

                  if (mc.options.leftKey.isPressed()) {
                     this.side = 1.0F;
                  }

                  if (mc.options.rightKey.isPressed()) {
                     this.side = -1.0F;
                  }

                  double angle = Math.atan2(mc.player.getZ() - this.target.getZ(), mc.player.getX() - this.target.getX());
                  angle += this.getMotion() / (double)Math.max(mc.player.distanceTo(this.target), this.getDistance.getMinValue()) * (double)this.side;
                  double x = this.target.getX() + (double)this.getDistance.getValue() * Math.cos(angle);
                  double z = this.target.getZ() + (double)this.getDistance.getValue() * Math.sin(angle);
                  double yaw = this.getYaw(mc.player, x, z);
                  double speed = this.getSpeed() * 0.93 + this.getGroundBoost();
                  if (this.potionBoost.getValue()) {
                     this.potionBoostLvl = 0;
                     mc.player.getStatusEffects().forEach((effect) -> {
                        if (effect.getEffectType().value() == StatusEffects.SPEED.value()) {
                           this.potionBoostLvl = Math.min(effect.getAmplifier() + 1, 4);
                        }

                     });
                  } else {
                     this.potionBoostLvl = 0;
                  }

                  double var10000;
                  switch (this.potionBoostLvl) {
                     case 1 -> var10000 = 1.01;
                     case 2 -> var10000 = 1.03;
                     case 3 -> var10000 = 1.38;
                     case 4 -> var10000 = 1.0475;
                     default -> var10000 = (double)1.0F;
                  }

                  double multiplier = var10000;
                  mc.player.setVelocity(speed * multiplier * -Math.sin(Math.toRadians(yaw)), mc.player.getVelocity().y, speed * multiplier * Math.cos(Math.toRadians(yaw)));
               }
            }
         }
      }
   }

   public LivingEntity getTarget() {
      return this.aura.getTarget();
   }

   private double getSpeed() {
      double del = mc.player.hasStatusEffect(StatusEffects.SLOWNESS) ? 0.7 : (double)1.0F;
      double speed = mc.player.isOnGround() ? 0.26 * del : 0.36 * del;
      return mc.player.getVelocity().y < (double)0.0F ? speed * 0.98 : speed;
   }

   private double getMotion() {
      return Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z);
   }

   private double getGroundBoost() {
      return (double)0.0F;
   }

   private double getYaw(LivingEntity entity, double x, double z) {
      return Math.toDegrees(Math.atan2(z - entity.getZ(), x - entity.getX())) - (double)90.0F;
   }

   public boolean strafes() {
      if (mc.player != null && mc.world != null) {
         if (mc.player.isSneaking()) {
            return false;
         } else if (!mc.player.isTouchingWater() && !mc.player.isInLava()) {
            BlockPos pos = mc.player.getBlockPos();
            if (mc.world.getBlockState(pos.up()).isAir() && mc.world.getBlockState(pos.down()).getBlock() == Blocks.WATER) {
               return false;
            } else if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
               return false;
            } else if (mc.world.getBlockState(pos.down()).getBlock() instanceof SoulSandBlock) {
               return false;
            } else if (mc.player.getAbilities().flying) {
               return false;
            } else {
               return !mc.player.hasStatusEffect(StatusEffects.LEVITATION);
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void onEnable() {
      this.aura = (Aura)CrolClient.INSTANCE.getModuleManager().getByClass(Aura.class);
      this.target = null;
   }
}
