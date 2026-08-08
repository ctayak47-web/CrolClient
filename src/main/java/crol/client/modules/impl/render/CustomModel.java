package crol.client.modules.impl.render;

import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.classes.Render2DEvent;
import crol.client.event.classes.RotationEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.event.interfaces.IRotateable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import crol.client.util.math.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import org.joml.Vector2f;

@Environment(EnvType.CLIENT)
public class CustomModel extends Module implements IRenderable2D, IRotateable, IReceivePacketable, IEnableable, IDisableable {
   public ModeSetting mode = ((ModeSetting)(new ModeSetting() {
      public void onChangeState(String val) {
         CustomModel.this.onDisable();
         CustomModel.this.onEnable();
         super.onChangeState(val);
      }
   }).name("Mode")).modes("Skeleton", "Zombie", "Pig", "Allay", "Wargen", "Piglin", "Phantom", "Vex", "Breeze", "Creeper", "Panda").value("Skeleton");
   private final BooleanSetting hands = ((BooleanSetting)(new BooleanSetting()).name("Hands")).value(false);
   private Vector2f rotate = new Vector2f(0.0F, 0.0F);
   private Vector2f endRotate = new Vector2f(0.0F, 0.0F);
   private Entity customModel;
   private boolean changeWorld = false;

   public CustomModel() {
      super(new ModuleInfo("ModuleInfo", Category.RENDER, "Изменяет прицел в игре"));
      this.addSetting(new Setting[]{this.mode, this.hands});
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (IUtil.mc.currentScreen instanceof DownloadingTerrainScreen) {
         this.changeWorld = true;
      }

      if (!(IUtil.mc.currentScreen instanceof DownloadingTerrainScreen) && this.changeWorld && IUtil.mc.world != null) {
         this.onDisable();
         this.onEnable();
         this.changeWorld = false;
      }

   }

   public void onRender2D(Render2DEvent event) {
      this.endRotate.x = MathUtil.fast(this.endRotate.x, this.rotate.x, 20.0F);
      this.endRotate.y = MathUtil.fast(this.endRotate.y, this.rotate.y, 20.0F);
      this.customModel.setPosition(IUtil.mc.player.getPos());
      this.customModel.setBoundingBox(this.calculateBoundingBox(this.customModel, 0.0F));
      this.customModel.setCustomName(Text.of(""));
      this.customModel.setYaw(this.endRotate.x);
      this.customModel.setPitch(this.endRotate.y);
      this.customModel.setHeadYaw(this.endRotate.x);
      this.customModel.setBodyYaw(this.endRotate.x);
      Entity var3 = this.customModel;
      if (var3 instanceof LivingEntity livingEntity) {
         livingEntity.handSwingProgress = IUtil.mc.player.handSwingProgress;
         livingEntity.lastHandSwingProgress = IUtil.mc.player.lastHandSwingProgress;
         livingEntity.limbAnimator.setSpeed(IUtil.mc.player.limbAnimator.getSpeed());
         if (this.hands.getValue()) {
            livingEntity.setStackInHand(Hand.MAIN_HAND, IUtil.mc.player.getOffHandStack());
            livingEntity.setStackInHand(Hand.OFF_HAND, IUtil.mc.player.getMainHandStack());
         } else {
            livingEntity.setStackInHand(Hand.MAIN_HAND, Items.AIR.getDefaultStack());
            livingEntity.setStackInHand(Hand.OFF_HAND, Items.AIR.getDefaultStack());
         }

         livingEntity.setPose(IUtil.mc.player.getPose());
      }

   }

   public Box calculateBoundingBox(Entity entity, float size) {
      double minX = entity.getX() - (double)size;
      double minY = this.customModel.getBoundingBox().minY;
      double minZ = entity.getZ() - (double)size;
      double maxX = entity.getX() + (double)size;
      double maxY = this.customModel.getBoundingBox().maxY;
      double maxZ = entity.getZ() + (double)size;
      return new Box(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public void onRotate(RotationEvent rotationEvent) {
      this.rotate.x = rotationEvent.getYaw();
      this.rotate.y = rotationEvent.getPitch();
   }

   public void onDisable() {
      if (this.customModel != null) {
         IUtil.mc.world.removeEntity(this.customModel.getId(), RemovalReason.DISCARDED);
      }
   }

   public void onEnable() {
      this.rotate = new Vector2f(IUtil.mc.player.getYaw(), IUtil.mc.player.getPitch());
      this.endRotate = new Vector2f(IUtil.mc.player.getYaw(), IUtil.mc.player.getPitch());
      this.customModel = new ZombieEntity(IUtil.mc.world);
      switch (this.mode.getValue()) {
         case "Skeleton" -> this.customModel = new SkeletonEntity(EntityType.SKELETON, IUtil.mc.world);
         case "Zombie" -> this.customModel = new ZombieEntity(IUtil.mc.world);
         case "Pig" -> this.customModel = new PigEntity(EntityType.PIG, IUtil.mc.world);
         case "Allay" -> this.customModel = new AllayEntity(EntityType.ALLAY, IUtil.mc.world);
         case "Wargen" -> this.customModel = new WardenEntity(EntityType.WARDEN, IUtil.mc.world);
         case "Piglin" -> this.customModel = new PiglinEntity(EntityType.PIGLIN, IUtil.mc.world);
         case "Phantom" -> this.customModel = new PhantomEntity(EntityType.PHANTOM, IUtil.mc.world);
         case "Vex" -> this.customModel = new VexEntity(EntityType.VEX, IUtil.mc.world);
         case "Breeze" -> this.customModel = new BreezeEntity(EntityType.BREEZE, IUtil.mc.world);
         case "Creeper" -> this.customModel = new CreeperEntity(EntityType.CREEPER, IUtil.mc.world);
         case "Panda" -> this.customModel = new PandaEntity(EntityType.PANDA, IUtil.mc.world);
      }

      IUtil.mc.world.addEntity(this.customModel);
   }

   public Entity getCustomModel() {
      return this.customModel;
   }

   public boolean isCustomEntity(Entity entity) {
      if (this.customModel == null) {
         return false;
      } else {
         return entity == this.customModel;
      }
   }
}
