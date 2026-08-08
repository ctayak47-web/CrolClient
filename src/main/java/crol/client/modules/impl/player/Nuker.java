package crol.client.modules.impl.player;

import crol.client.CrolClient;
import crol.client.event.classes.MoveCorrectionEvent;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.classes.RotationEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IMoveCorrectionable;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.event.interfaces.IRotateable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import crol.client.util.render.RenderUtil3D;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

@Environment(EnvType.CLIENT)
public class Nuker extends Module implements IRotateable, IMoveCorrectionable, ITickable, IUtil, IDisableable, IRenderable3D {
   private final FloatSetting distanceY = ((FloatSetting)(new FloatSetting()).name("DistanceY")).value(2.0F).minValue(1.0F).maxValue(5.0F).incriment(1.0F);
   private final FloatSetting distanceXZ = ((FloatSetting)(new FloatSetting()).name("DistanceXZ")).value(2.0F).minValue(1.0F).maxValue(5.0F).incriment(1.0F);
   private Vector2d rotateVector = new Vector2d((double)0.0F, (double)0.0F);
   private BlockPos blockPos = null;
   private float lastYaw;
   private float lastPitch;
   private final Random random = new Random();
   private long lastUpdateTime = 0L;
   private boolean updated = false;

   public Nuker() {
      super(new ModuleInfo("Nuker", Category.PLAYER, "Ломает блоки вокруг"));
      this.addSetting(new Setting[]{this.distanceY, this.distanceXZ});
   }

   public void onRotate(RotationEvent e) {
      e.setYaw((float)this.rotateVector.x);
      e.setPitch((float)this.rotateVector.y);
      if (mc.player != null) {
         CrolClient.INSTANCE.setBodyPitch((float)this.rotateVector.y);
         mc.player.setHeadYaw((float)this.rotateVector.x);
         mc.player.bodyYaw = (float)this.rotateVector.x;
      }

   }

   public void onTick(TickEvent event) {
      if (mc.player != null && mc.world != null) {
         if (this.blockPos != null) {
            this.rotations(this.blockPos);
         } else {
            this.changeRotToDefault();
         }

         if (this.blockPos != null && mc.world.getBlockState(this.blockPos).getBlock() instanceof AirBlock && !this.updated) {
            this.lastUpdateTime = System.currentTimeMillis();
            this.updated = true;
         }

         if (System.currentTimeMillis() - this.lastUpdateTime >= 100L) {
            this.updateBlocks();
            this.updated = false;
         }

         if (this.blockPos != null && !(mc.world.getBlockState(this.blockPos).getBlock() instanceof AirBlock) && this.isLookingAtBlock(this.blockPos, 5.0F)) {
            mc.interactionManager.updateBlockBreakingProgress(this.blockPos, Direction.fromHorizontalDegrees(this.rotateVector.x));
            mc.player.swingHand(Hand.MAIN_HAND);
         }

      }
   }

   public void moveCorrection(MoveCorrectionEvent event) {
      event.setYaw((float)this.rotateVector.x);
   }

   public void onDisable() {
      this.blockPos = null;
      if (mc.player != null) {
         this.changeRotToDefault();
      }

   }

   private void updateBlocks() {
      Vec3d pos = mc.player.getPos();
      int dXZ = (int)this.distanceXZ.getValue();
      int dY = (int)this.distanceY.getValue();
      BlockPos closest = null;
      double closestDist = Double.MAX_VALUE;

      for(int y = 0; y < dY; ++y) {
         for(int x = -dXZ; x <= dXZ; ++x) {
            for(int z = -dXZ; z <= dXZ; ++z) {
               BlockPos bp = BlockPos.ofFloored(pos.x - (double)x, pos.y + (double)y, pos.z - (double)z);
               Block block = mc.world.getBlockState(bp).getBlock();
               if (block != Blocks.AIR && block != Blocks.WATER && block != Blocks.LAVA && block != Blocks.BEDROCK) {
                  double dist = pos.squaredDistanceTo((double)bp.getX() + (double)0.5F, (double)bp.getY() + (double)0.5F, (double)bp.getZ() + (double)0.5F);
                  if (dist < closestDist) {
                     closestDist = dist;
                     closest = bp;
                  }
               }
            }
         }
      }

      this.blockPos = closest;
   }

   private void rotations(BlockPos target) {
      float currentYaw = (float)this.rotateVector.x;
      float currentPitch = (float)this.rotateVector.y;
      Vec3d targetVec = new Vec3d((double)target.getX() + (double)0.5F, (double)target.getY() + (double)0.5F, (double)target.getZ() + (double)0.5F);
      Vec3d eyePos = mc.player.getCameraPosVec(1.0F);
      Vec3d delta = targetVec.subtract(eyePos);
      float yawToTarget = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - (double)90.0F);
      float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z))));
      float yawDelta = MathHelper.wrapDegrees(yawToTarget - currentYaw);
      float pitchDelta = MathHelper.wrapDegrees(pitchToTarget - currentPitch);
      float speed = 4.5F + ThreadLocalRandom.current().nextFloat(1.0F) * 0.5F;
      float yawStep = MathHelper.clamp(yawDelta / speed, -40.0F, 40.0F);
      float pitchStep = MathHelper.clamp(pitchDelta / speed, -30.0F, 30.0F);
      float yaw = currentYaw + yawStep;
      float pitch = MathHelper.clamp(currentPitch + pitchStep, -89.0F, 89.0F);
      float gcd = this.getGCDValue();
      yaw -= (yaw - currentYaw) % gcd;
      pitch -= (pitch - currentPitch) % gcd;
      this.rotateVector = new Vector2d((double)yaw, (double)pitch);
   }

   private void changeRotToDefault() {
      float currentYaw = (float)this.rotateVector.x;
      float currentPitch = (float)this.rotateVector.y;
      float playerYaw = mc.player.getYaw();
      float playerPitch = mc.player.getPitch();
      float yawDiff = Math.abs(MathHelper.wrapDegrees(playerYaw - currentYaw));
      float pitchDiff = Math.abs(MathHelper.wrapDegrees(playerPitch - currentPitch));
      if (yawDiff < 0.5F && pitchDiff < 0.5F) {
         this.rotateVector = new Vector2d((double)playerYaw, (double)playerPitch);
      } else {
         float smoothing = 5.5F + this.random.nextFloat() * 0.5F;
         float yaw = this.smoothStep(currentYaw, playerYaw, smoothing);
         float pitch = this.smoothStep(currentPitch, playerPitch, smoothing);
         this.rotateVector = new Vector2d((double)yaw, (double)pitch);
      }
   }

   private float smoothStep(float current, float target, float factor) {
      float delta = MathHelper.wrapDegrees(target - current);
      return current + delta / factor;
   }

   private float getGCDValue() {
      float sensitivity = (float)((Double)mc.options.getMouseSensitivity().getValue() * (double)0.6F + (double)0.2F);
      float gcd = sensitivity * sensitivity * sensitivity * 8.0F;
      return gcd * ThreadLocalRandom.current().nextFloat(0.07F) + 0.1F;
   }

   public void onRender3D(Render3DEvent event) {
      if (this.blockPos != null && mc.world != null && !(mc.world.getBlockState(this.blockPos).getBlock() instanceof AirBlock)) {
         Box box = new Box((double)this.blockPos.getX(), (double)this.blockPos.getY(), (double)this.blockPos.getZ(), (double)(this.blockPos.getX() + 1), (double)(this.blockPos.getY() + 1), (double)(this.blockPos.getZ() + 1));
         RenderUtil3D.getInstance().drawBox(box, CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB(), 1.5F);
      }

   }

   private boolean isLookingAtBlock(BlockPos target, float threshold) {
      Vec3d targetVec = new Vec3d((double)target.getX() + (double)0.5F, (double)target.getY() + (double)0.5F, (double)target.getZ() + (double)0.5F);
      Vec3d eyePos = mc.player.getCameraPosVec(1.0F);
      Vec3d delta = targetVec.subtract(eyePos);
      float yawToTarget = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(delta.z, delta.x)) - (double)90.0F);
      float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z))));
      float yawDiff = Math.abs(MathHelper.wrapDegrees(yawToTarget - (float)this.rotateVector.x));
      float pitchDiff = Math.abs(MathHelper.wrapDegrees(pitchToTarget - (float)this.rotateVector.y));
      return yawDiff <= threshold && pitchDiff <= threshold;
   }
}
