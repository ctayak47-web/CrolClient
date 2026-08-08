package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.CameraPositionEvent;
import crol.client.modules.impl.render.SmoothCamera;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({Camera.class})
public abstract class CameraMixin {
   @Shadow
   private Vec3d pos;
   private float smoothYaw = 0.0F;
   private float smoothPitch = 0.0F;
   private boolean initialized = false;
   private long lastTimeNanos = 0L;
   @Shadow
   @Final
   private BlockPos.Mutable blockPos;

   @Shadow
   protected abstract void setRotation(float var1, float var2);

   @Inject(
      method = {"update"},
      at = {@At("TAIL")}
   )
   private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
      SmoothCamera smoothCamera = (SmoothCamera)CrolClient.INSTANCE.getModuleManager().getByClass(SmoothCamera.class);
      if (smoothCamera.isEnabled() && MinecraftClient.getInstance().options.getPerspective() == Perspective.FIRST_PERSON) {
         float targetYaw = focusedEntity.getYaw();
         float targetPitch = focusedEntity.getPitch();
         if (thirdPerson && inverseView) {
            targetYaw += 180.0F;
            targetPitch = -targetPitch;
         }

         if (!this.initialized) {
            this.smoothYaw = targetYaw;
            this.smoothPitch = targetPitch;
            this.lastTimeNanos = System.nanoTime();
            this.initialized = true;
            return;
         }

         long now = System.nanoTime();
         float dt = (float)(now - this.lastTimeNanos) / 1.0E9F;
         this.lastTimeNanos = now;
         dt = Math.min(dt, 0.1F);
         float speed = smoothCamera.getDelay().getValue();
         float factor = 1.0F - (float)Math.exp((double)(-speed * dt));
         float dyaw = wrapDegrees(targetYaw - this.smoothYaw);
         this.smoothYaw += dyaw * factor;
         this.smoothPitch += (targetPitch - this.smoothPitch) * factor;
         this.setRotation(this.smoothYaw, this.smoothPitch);
      }

   }

   private static float wrapDegrees(float deg) {
      deg %= 360.0F;
      if (deg >= 180.0F) {
         deg -= 360.0F;
      }

      if (deg < -180.0F) {
         deg += 360.0F;
      }

      return deg;
   }

   @Inject(
      method = {"setPos(Lnet/minecraft/util/math/Vec3d;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void posHook(Vec3d pos, CallbackInfo ci) {
      CameraPositionEvent event = new CameraPositionEvent(pos);
      CrolClient.INSTANCE.getEventManager().hookEvent(event);
      this.pos = pos = event.getPos();
      this.blockPos.set(pos.x, pos.y, pos.z);
      ci.cancel();
   }
}
