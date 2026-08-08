package crol.client.modules.impl.combat;

import crol.client.CrolClient;
import crol.client.event.classes.FireworkEvent;
import crol.client.event.classes.InputEvent;
import crol.client.event.classes.MoveCorrectionEvent;
import crol.client.event.classes.Render2DEvent;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.classes.RotationEvent;
import crol.client.event.classes.SendPacketEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.classes.TravelEvent;
import crol.client.event.interfaces.IFireworkable;
import crol.client.event.interfaces.IInputable;
import crol.client.event.interfaces.IMoveCorrectionable;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.event.interfaces.IRotateable;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.event.interfaces.ITickable;
import crol.client.event.interfaces.ITravelable;
import crol.client.mixins.other.IClientCommandC2SPacketMixin;
import crol.client.mixins.other.IPlayerMoveC2SPacket;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.impl.movement.Sprint;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.aura.AuraUtil;
import crol.client.util.aura.Gcd;
import crol.client.util.aura.esp.EspMode;
import crol.client.util.aura.esp.impl.CircleMode;
import crol.client.util.aura.esp.impl.DefaultMode;
import crol.client.util.aura.esp.impl.FireMode;
import crol.client.util.aura.rotate.GRURotation;
import crol.client.util.math.TimerUtil;
import crol.client.util.player.MovementUtil;
import crol.client.util.player.PacketUtil;
import crol.client.util.player.inventory.InventoryUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;

@Environment(EnvType.CLIENT)
public class Aura extends Module implements ITickable, IRenderable3D, IUtil, ITravelable, IFireworkable, IInputable, ISendPacketable, IEnableable, IRotateable, IMoveCorrectionable, IRenderable2D, IDisableable {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("ReallyWorld").modes("ReallyWorld");
   private final FloatSetting preaim = ((FloatSetting)(new FloatSetting()).name("PreAim")).value(0.5F).minValue(0.0F).maxValue(2.0F).incriment(0.1F);
   private final FloatSetting getDistance = ((FloatSetting)(new FloatSetting()).name("Distance")).value(3.0F).minValue(3.0F).maxValue(6.0F).incriment(0.1F);
   private final ModeSetting espMode = ((ModeSetting)(new ModeSetting() {
      public void onChangeState(String val) {
         switch (val) {
            case "Marker":
               Aura.this.curretEspMode = new DefaultMode();
               ((DefaultMode)Aura.this.curretEspMode).setMode(Aura.this.getMode(Aura.this.markerMode.getValue()));
               break;
            case "Fire":
               Aura.this.curretEspMode = new FireMode();
               break;
            case "Circle":
               Aura.this.curretEspMode = new CircleMode();
         }

      }
   }).name("TargetEsp")).value("Marker").modes("Marker", "Fire", "Circle");
   private final ModeSetting markerMode = ((ModeSetting)(new ModeSetting() {
      public boolean isVisible() {
         return Aura.this.espMode.is("Marker");
      }

      public void onChangeState(String val) {
         ((DefaultMode)Aura.this.curretEspMode).setMode(Aura.this.getMode(val));
         super.onChangeState(val);
      }
   }).name("MarketMode")).value("Type 1").modes("Type 1", "Type 2", "Type 3", "Type 4");
   private final MultiBoxSetting targets = ((MultiBoxSetting)(new MultiBoxSetting()).name("Targets")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Players")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Nakeds")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Mobs")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Animals")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Friends")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Naked invisibles")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Invisibles")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Villager")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Bots")).value(false));
   private final MultiBoxSetting sort = ((MultiBoxSetting)(new MultiBoxSetting()).name("Sort")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Hp")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Effects")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Distance")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Armor")).value(false));
   public ModeSetting correction = ((ModeSetting)(new ModeSetting()).name("Correction")).value("Free").modes("Focused", "Free", "None");
   private final MultiBoxSetting options = ((MultiBoxSetting)(new MultiBoxSetting()).name("Options")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Walls")).value(false), ((BooleanSetting)(new BooleanSetting()).name("OnlyCrit")).value(false), ((BooleanSetting)(new BooleanSetting()).name("NoEatAttack")).value(false), ((BooleanSetting)(new BooleanSetting()).name("ElytraTarget")).value(false), ((BooleanSetting)(new BooleanSetting()).name("OffSprint")).value(false), ((BooleanSetting)(new BooleanSetting()).name("ShieldBreaker")).value(false), ((BooleanSetting)(new BooleanSetting()).name("StopUseItem")).value(false), ((BooleanSetting)(new BooleanSetting()).name("MakeBoost")).value(false), ((BooleanSetting)(new BooleanSetting()).name("SaveTarget")).value(false), ((BooleanSetting)(new BooleanSetting()).name("CameraTarget")).value(false));
   public ModeSetting offSprintMode = ((ModeSetting)(new ModeSetting() {
      public boolean isVisible() {
         return Aura.this.options.getValueByName("OffSprint");
      }
   }).name("OffSprintMode")).value("Default").modes("Default", "Legit");
   private final BooleanSetting smartCrits = ((BooleanSetting)(new BooleanSetting() {
      public boolean isVisible() {
         return Aura.this.options.getValueByName("OnlyCrit");
      }
   }).name("SmartCrits")).value(false);
   private final BooleanSetting miniJump = ((BooleanSetting)(new BooleanSetting() {
      public boolean isVisible() {
         return Aura.this.options.getValueByName("OnlyCrit");
      }
   }).name("MiniJump")).value(false);
   private final BooleanSetting autoFireWork = ((BooleanSetting)(new BooleanSetting() {
      public boolean isVisible() {
         return Aura.this.options.getValueByName("ElytraTarget");
      }
   }).name("AutoFirework")).value(false);
   private final FloatSetting fireworkCooldown = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return Aura.this.autoFireWork.isVisible() && Aura.this.autoFireWork.getValue();
      }
   }).name("FireworkCooldown")).value(1500.0F).minValue(500.0F).maxValue(3000.0F).incriment(50.0F);
   private final BooleanSetting betterTarget = ((BooleanSetting)(new BooleanSetting() {
      public boolean isVisible() {
         return Aura.this.options.getValueByName("ElytraTarget");
      }
   }).name("BetterTarget")).value(false);
   private final FloatSetting elytraDistance = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return Aura.this.options.getValueByName("ElytraTarget");
      }
   }).name("ElytraDistance")).value(15.0F).minValue(10.0F).maxValue(30.0F).incriment(1.0F);
   private EspMode curretEspMode;
   private LivingEntity target;
   private final TimerUtil fireWork = new TimerUtil();
   private final TimerUtil attack = new TimerUtil();
   private Vector2f rotate = new Vector2f(0.0F, 0.0F);
   private float lastYaw;
   private float lastPitch;
   private Sprint sprint;
   private SyncTps syncTps;
   private AutoPotion autoPotion;
   private Vec3d currentAimOffset;
   private Vec3d targetAimOffset;
   private long nextAimOffsetUpdate;
   private LivingEntity lastAimTarget;
   private float prevOutYaw;
   private float prevOutPitch;
   private float rndInertia;
   private float rndMicroMul;
   private float rndMicroInert;
   private float rndYawFloor;
   private float rndPitchFloor;
   private float rndDriftYaw;
   private float rndDriftPitch;
   private float rndOscDamp;
   private float rndGcdYaw;
   private float rndGcdPitch;
   private long nextRndUpdate;
   private boolean isAimed;

   public Aura() {
      super(new ModuleInfo("Aura", Category.COMBAT, "Атакует противников рядом с игроком"));
      this.currentAimOffset = Vec3d.ZERO;
      this.targetAimOffset = Vec3d.ZERO;
      this.nextAimOffsetUpdate = 0L;
      this.lastAimTarget = null;
      this.prevOutYaw = 0.0F;
      this.prevOutPitch = 0.0F;
      this.rndInertia = 0.12F;
      this.rndMicroMul = 0.55F;
      this.rndMicroInert = 0.3F;
      this.rndYawFloor = 0.3F;
      this.rndPitchFloor = 0.18F;
      this.rndDriftYaw = 0.05F;
      this.rndDriftPitch = 0.04F;
      this.rndOscDamp = 0.3F;
      this.rndGcdYaw = 0.86F;
      this.rndGcdPitch = 0.86F;
      this.nextRndUpdate = 0L;
      this.isAimed = false;
      this.curretEspMode = new DefaultMode();
      this.addSetting(new Setting[]{this.mode, this.preaim, this.getDistance, this.espMode, this.markerMode, this.targets, this.sort, this.correction, this.options, this.offSprintMode, this.smartCrits, this.miniJump, this.autoFireWork, this.fireworkCooldown, this.betterTarget, this.elytraDistance});
   }

   private String getMode(String mode) {
      switch (mode) {
         case "Type 1" -> {
            return "targetesp1.jpg";
         }
         case "Type 2" -> {
            return "targetesp2.jpg";
         }
         case "Type 3" -> {
            return "targetesp3.png";
         }
         case "Type 4" -> {
            return "targetesp4.png";
         }
         default -> {
            return "targetesp1.jpg";
         }
      }
   }

   public void onTick(TickEvent event) {
      if (this.target == null || !this.isValid(this.target) || !this.options.getValueByName("SaveTarget")) {
         this.updateTarget();
      }

      if (mc.player.isGliding() && this.target != null && this.options.getValueByName("ElytraTarget") && this.fireWork.isReached((long)this.fireworkCooldown.getValue()) && this.autoFireWork.getValue()) {
         InventoryUtil.sendFireWork(this.rotate.x, this.rotate.y);
         this.fireWork.reset();
      }

      if (this.target != null) {
         if (this.options.getValueByName("OnlyCrit") && this.miniJump.getValue() && mc.player.getAttackCooldownProgress(this.syncTps.isEnabled() ? SyncTps.adjustTicks : 2.0F) == 1.0F && mc.player.isOnGround() && this.getTarget() != null) {
            mc.player.setVelocity((double)0.0F, 0.04, (double)0.0F);
         }

         label75: {
            boolean isElytraPvP = mc.player.isGliding() && this.options.getValueByName("ElytraTarget");
            if (this.options.getValueByName("ElytraTarget")) {
               if (mc.player.isGliding()) {
                  if (!(this.target.distanceTo(mc.player) <= this.elytraDistance.getValue())) {
                     break label75;
                  }
               } else if (!(this.target.distanceTo(mc.player) <= this.getDistance.getValue() + this.preaim.getValue())) {
                  break label75;
               }
            }

            if (!isElytraPvP) {
               this.rotations(this.target);
            }
         }

         if (!this.options.getValueByName("NoEatAttack") || !this.isUseItems()) {
            this.attack();
         }
      } else {
         this.rotate = new Vector2f(mc.player.getYaw(), mc.player.getPitch());
      }

      if (this.options.getValueByName("CameraTarget") && mc.targetedEntity != null && this.target != mc.targetedEntity) {
         Entity var3 = mc.targetedEntity;
         if (var3 instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)var3;
            if (this.isValid(living)) {
               this.target = living;
            }
         }
      }

   }

   private boolean isUseItems() {
      if (this.options.getValueByName("StopUseItem") && mc.player.getBlockingItem() != null && mc.player.getBlockingItem().getItem() == Items.SHIELD) {
         return false;
      } else {
         return mc.player.isUsingItem();
      }
   }

   public void onRender3D(Render3DEvent event) {
      if (this.target != null && mc.player.distanceTo(this.target) <= this.getDistance.getValue() + this.preaim.getValue()) {
         this.curretEspMode.render(this.target, event.getMatrixStack());
      }

   }

   private void updateTarget() {
      List<LivingEntity> targets = new ArrayList();

      for(Entity entity : mc.world.getEntities()) {
         if (entity instanceof LivingEntity living) {
            if (this.isValid(living)) {
               targets.add(living);
            }
         }
      }

      if (targets.isEmpty()) {
         this.target = null;
      } else if (targets.size() == 1) {
         this.target = (LivingEntity)targets.get(0);
      } else {
         targets.sort(Comparator.comparingDouble((entityx) -> AuraUtil.entity(entityx, this.sort.getValueByName("Hp"), this.sort.getValueByName("Armor"), this.sort.getValueByName("Distance"), (double)(this.getDistance.getValue() + this.preaim.getValue()), this.sort.getValueByName("Effects"))));
         this.target = (LivingEntity)targets.get(0);
      }

   }

   private boolean isValid(LivingEntity entity) {
      if (entity != null && entity != mc.player) {
         if (entity.isAlive() && !(entity.getHealth() <= 0.0F)) {
            if (this.options.getValueByName("ElytraTarget") && mc.player.isGliding()) {
               return entity instanceof PlayerEntity;
            } else {
               AntiBot antiBot = (AntiBot)CrolClient.INSTANCE.getModuleManager().getByClass(AntiBot.class);
               if (!this.targets.getValueByName("Bots") && antiBot.isBot(entity)) {
                  return false;
               } else if (entity instanceof ClientPlayerEntity) {
                  return false;
               } else if (!this.options.getValueByName("Walls") && !mc.player.canSee(entity)) {
                  return false;
               } else {
                  if ((mc.player.isGliding() || mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) && this.options.getValueByName("ElytraTarget")) {
                     if (mc.player.distanceTo(entity) > this.elytraDistance.getValue()) {
                        return false;
                     }
                  } else if (mc.player.distanceTo(entity) >= this.getDistance.getValue() + this.preaim.getValue()) {
                     return false;
                  }

                  if (entity instanceof PlayerEntity) {
                     PlayerEntity p = (PlayerEntity)entity;
                     if (!this.targets.getValueByName("Friends") && CrolClient.INSTANCE.getFriendManager().isFriend(p.getName().getString())) {
                        return false;
                     }

                     if (p.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) {
                        return false;
                     }
                  }

                  if (entity instanceof PlayerEntity && !this.targets.getValueByName("Players")) {
                     return false;
                  } else if (entity instanceof VillagerEntity && !this.targets.getValueByName("Villager")) {
                     return false;
                  } else if (entity instanceof PlayerEntity && entity.getArmor() == 0 && !this.targets.getValueByName("Nakeds")) {
                     return false;
                  } else if (entity instanceof PlayerEntity && entity.isInvisible() && entity.getArmor() == 0 && !this.targets.getValueByName("Naked invisibles")) {
                     return false;
                  } else if (entity instanceof PlayerEntity && entity.isInvisible() && !this.targets.getValueByName("Invisibles")) {
                     return false;
                  } else if (entity instanceof Monster && !this.targets.getValueByName("Mobs")) {
                     return false;
                  } else if (entity instanceof AnimalEntity && !this.targets.getValueByName("Animals")) {
                     return false;
                  } else {
                     return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
                  }
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void rotations(LivingEntity targetEntity) {
      if (mc.player.isGliding() && this.options.getValueByName("ElytraTarget")) {
         Vec3d vec = this.target.getPos().add((double)0.0F, MathHelper.clamp(mc.player.getEyeY() - this.target.getY(), (double)0.0F, (double)(this.target.getHeight() * (mc.player.distanceTo(this.target) / this.getDistance.getValue()))), (double)0.0F).subtract(mc.player.getCameraPosVec(1.0F));
         if (!targetEntity.isGliding()) {
            vec.add((double)0.0F, (double)(this.getDistance.getValue() - mc.player.distanceTo(this.target)) * (double)3.5F, (double)0.0F);
         }

         float yawToTarget = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - (double)90.0F);
         float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))));
         float yawDelta = MathHelper.wrapDegrees(yawToTarget - this.rotate.x);
         float pitchDelta = MathHelper.wrapDegrees(pitchToTarget - this.rotate.y);
         int roundedYaw = (int)yawDelta;
         float s = (new Random()).nextFloat(0.53F);
         float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1.0F), 6.0F + s);
         float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1.0F), 5.07F + s);
         if (Math.abs(clampedYaw - this.lastYaw) <= 3.0F) {
            clampedYaw = this.lastYaw + 2.5F;
         }

         float yaw = this.rotate.x + (yawDelta > 0.0F ? clampedYaw : -clampedYaw);
         float pitch = MathHelper.clamp(this.rotate.y + (pitchDelta > 0.0F ? clampedPitch : -clampedPitch), -89.0F, 89.0F);
         float gcd = Gcd.getGCDValue();
         yaw -= (yaw - this.rotate.x) % gcd;
         pitch -= (pitch - this.rotate.y) % gcd;
         this.rotate = new Vector2f(yaw, pitch);
         this.lastYaw = clampedYaw;
         this.lastPitch = clampedPitch;
      } else {
         Vec3d vec = this.target.getPos().add((double)0.0F, MathHelper.clamp(mc.player.getEyeY() - this.target.getY(), (double)0.0F, (double)(this.target.getHeight() * (mc.player.distanceTo(this.target) / (this.getDistance.getValue() + this.preaim.getValue())))), (double)0.0F).subtract(mc.player.getCameraPosVec(1.0F));
         float yawToTarget = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - (double)90.0F);
         float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))));
         float yawDelta = MathHelper.wrapDegrees(yawToTarget - this.rotate.x);
         float pitchDelta = MathHelper.wrapDegrees(pitchToTarget - this.rotate.y);
         int roundedYaw = (int)yawDelta;
         switch (this.mode.getValue()) {
            case "ReallyWorld":
               Vec3d targetTorso = this.target.getPos().add((double)0.0F, (double)(this.target.getHeight() / 2.0F), (double)0.0F);
               Vec3d eyePos = mc.player.getEyePos();
               vec = targetTorso.subtract(eyePos);
               yawToTarget = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - (double)90.0F);
               pitchToTarget = (float)(-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))));
               yawDelta = MathHelper.wrapDegrees(yawToTarget - this.rotate.x);
               pitchDelta = MathHelper.wrapDegrees(pitchToTarget - this.rotate.y);
               float clampedYaw2 = Math.max(Math.abs(yawDelta), 0.0F);
               float clampedPitch2 = Math.max(Math.abs(pitchDelta), 0.0F);
               float yaw = this.rotate.x + (yawDelta > 0.0F ? clampedYaw2 : -clampedYaw2) + ThreadLocalRandom.current().nextFloat(-0.1F, 2.8F);
               float pitch = MathHelper.clamp(this.rotate.y + (pitchDelta > 0.0F ? clampedPitch2 : -clampedPitch2) + ThreadLocalRandom.current().nextFloat(-0.1F, 0.1F), -89.0F, 89.0F);
               float gcd2 = Gcd.getGCD();
               yaw -= (yaw - this.rotate.x) % gcd2;
               pitch -= (pitch - this.rotate.y) % gcd2;
               this.rotate = new Vector2f(yaw, pitch);
         }
      }

   }

   private void disableSprint() {
      if (!mc.player.isGliding() && (CrolClient.INSTANCE.getRotationManager().isServerSprint() || mc.player.isSprinting()) && this.options.getValueByName("OffSprint")) {
         switch (this.offSprintMode.getValue()) {
            case "Default":
               mc.player.input.movementSideways = 0.0F;
               mc.player.input.movementForward = 0.0F;
               mc.player.setSprinting(false);
               mc.options.sprintKey.setPressed(false);
               PacketUtil.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
               break;
            case "Legit":
               mc.player.input.movementForward = 0.0F;
               mc.player.input.movementSideways = 0.0F;
               mc.options.sprintKey.setPressed(false);
               mc.player.setSprinting(false);
               mc.options.sprintKey.setPressed(false);
         }
      }

   }

   private void attack() {
      boolean canAttack = this.options.getValueByName("OnlyCrit") ? AuraUtil.canAttack(this.smartCrits.getValue(), this.syncTps) : (double)mc.player.getAttackCooldownProgress(2.0F) >= 0.97;
      if (canAttack && this.target.distanceTo(mc.player) <= this.getDistance.getValue() && this.attack.isReached((long)(540 + ThreadLocalRandom.current().nextInt(30)))) {
         int b = mc.player.getInventory().selectedSlot;
         boolean swapped = false;
         if (this.options.getValueByName("MakeBoost")) {
            for(int i = 0; i < 9; ++i) {
               if (mc.player.getInventory().getStack(i).getItem() == Items.MACE && !swapped) {
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                  swapped = true;
               }
            }
         }

         this.sprint.setCanSprint(false);
         this.disableSprint();
         if (this.options.getValueByName("StopUseItem") && mc.player.getBlockingItem() != null && mc.player.getBlockingItem().getItem() == Items.SHIELD) {
            mc.interactionManager.stopUsingItem(mc.player);
         }

         mc.interactionManager.attackEntity(mc.player, this.target);
         mc.player.swingHand(Hand.MAIN_HAND);
         if (this.options.getValueByName("MakeBoost") && swapped) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(b));
         }

         if (this.options.getValueByName("ShieldBreaker")) {
            LivingEntity var5 = this.target;
            if (var5 instanceof PlayerEntity) {
               PlayerEntity player = (PlayerEntity)var5;
               AuraUtil.breakShield(player);
            }
         }

         if (mc.player.isGliding() && this.options.getValueByName("ElytraTarget") && (this.fireWork.isReached(1200L) || mc.player.isGliding() && this.options.getValueByName("ElytraTarget") && this.betterTarget.getValue())) {
            InventoryUtil.sendFireWork(this.rotate.x, this.rotate.y);
            this.fireWork.reset();
         }

         this.sprint.getTimerUtil().reset();
         this.sprint.setCanSprint(true);
         this.attack.reset();
      }

   }

   public void onFirework(FireworkEvent fireworkEvent) {
      if (this.options.getValueByName("ElytraTarget") || this.target != null) {
         fireworkEvent.setYaw((double)this.rotate.x);
         fireworkEvent.setPitch((double)this.rotate.y);
      }

   }

   public void onTravel(TravelEvent travelEvent) {
      if (this.options.getValueByName("ElytraTarget") || this.target != null) {
         travelEvent.setPitch((double)this.rotate.y);
      }

   }

   public void onInput(InputEvent event) {
      if (this.target != null && this.correction.is("Free")) {
         MovementUtil.fixMovement(event, this.rotate.x);
      }

   }

   public void onSendPacket(SendPacketEvent sendPacketEvent) {
      Packet var3 = sendPacketEvent.getPacket();
      if (var3 instanceof PlayerMoveC2SPacket playerMoveC2SPacket) {
         ((IPlayerMoveC2SPacket)playerMoveC2SPacket).setPitch(this.rotate.y);
         ((IPlayerMoveC2SPacket)playerMoveC2SPacket).setYaw(this.rotate.x);
      }

      if (!this.sprint.isCanSprint()) {
         var3 = sendPacketEvent.getPacket();
         if (var3 instanceof ClientCommandC2SPacket) {
            ClientCommandC2SPacket clientCommandC2SPacket = (ClientCommandC2SPacket)var3;
            if (clientCommandC2SPacket.getMode() == Mode.START_SPRINTING) {
               ((IClientCommandC2SPacketMixin)clientCommandC2SPacket).setMode(Mode.STOP_SPRINTING);
            }
         }
      }

   }

   public void onEnable() {
      this.autoPotion = (AutoPotion)CrolClient.INSTANCE.getModuleManager().getByClass(AutoPotion.class);
      this.syncTps = (SyncTps)CrolClient.INSTANCE.getModuleManager().getByClass(SyncTps.class);
      GRURotation.get().reload();
      this.rotate = new Vector2f(mc.player.getYaw(), mc.player.getPitch());
      this.sprint = (Sprint)CrolClient.INSTANCE.getModuleManager().getByClass(Sprint.class);
      this.target = null;
   }

    public void onRotate(RotationEvent event) {
       if (this.target == null) return;
        event.setYaw(this.rotate.x);
        event.setPitch(this.rotate.y);
    }

   public void moveCorrection(MoveCorrectionEvent event) {
      if (!this.correction.is("None")) {
         event.setYaw(this.rotate.x);
         event.setPitch(this.rotate.y);
      }

   }

   public LivingEntity getTarget() {
      return this.target;
   }

   public void onRender2D(Render2DEvent event) {
      boolean isElytraPvP = mc.player.isGliding() && this.options.getValueByName("ElytraTarget");
      if (this.target != null) {
         if (this.options.getValueByName("ElytraTarget")) {
            if (mc.player.isGliding()) {
               if (!(this.target.distanceTo(mc.player) <= this.elytraDistance.getValue())) {
                  return;
               }
            } else if (!(this.target.distanceTo(mc.player) <= this.getDistance.getValue() + this.preaim.getValue())) {
               return;
            }
         }

         if (isElytraPvP) {
            this.rotations(this.target);
         }
      }

   }

   public Vector2f getRotate() {
      return this.rotate;
   }

   public void onDisable() {
      this.target = null;
   }
}
