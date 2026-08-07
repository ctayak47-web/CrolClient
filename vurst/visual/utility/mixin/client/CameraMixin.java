
package vurst.visual.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.Entity;
import net.minecraft.BlockView;
import net.minecraft.BlockPos;
import net.minecraft.Vec3d;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.Camera;
import net.minecraft.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.base.events.impl.render.EventCamera;
import vurst.visual.base.events.impl.render.EventCameraPosition;
import vurst.visual.client.modules.impl.render.BabyModel;
import vurst.visual.utility.game.player.rotation.Rotation;
import vurst.visual.utility.interfaces.IMinecraft;

@Mixin(value={Camera.class})
public abstract class CameraMixin
implements IMinecraft {
    private static final double BABY_MODEL_CAMERA_EPSILON = 0.03;
    @Shadow
    private Vec3d pos;
    @Shadow
    @Final
    private BlockPos.Mutable blockPos;
    @Shadow
    private float yaw;
    @Shadow
    private float pitch;

    @Shadow
    protected abstract void setRotation(float var1, float var2);

    @Shadow
    protected abstract void moveBy(float var1, float var2, float var3);

    @Shadow
    protected abstract float clipToSpace(float var1);

    @Inject(method={"update"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void updateHook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        ClientPlayerEntity player;
        EventCamera event = new EventCamera(false, 4.0f, new Rotation(this.yaw, this.pitch));
        EventManager.call(event);
        this.applyBabyModelCameraOffset(focusedEntity, thirdPerson);
        Rotation angle = event.getAngle();
        if (event.isCancelled() && focusedEntity instanceof ClientPlayerEntity && !(player = (ClientPlayerEntity)focusedEntity).isSleeping()) {
            float pitch = inverseView ? -angle.getPitch() : angle.getPitch();
            float yaw = angle.getYaw() - (float)(inverseView ? 180 : 0);
            float distance = Math.max(0.0f, event.getDistance());
            this.setRotation(yaw, pitch);
            if (distance > 1.0E-4f) {
                this.moveBy(event.isCameraClip() ? -distance : -this.clipToSpace(distance), 0.0f, 0.0f);
            }
            ci.cancel();
        }
    }

    @Inject(method={"setPos(Lnet/minecraft/util/math/Vec3d;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void posHook(Vec3d pos, CallbackInfo ci) {
        EventCameraPosition event = new EventCameraPosition(pos);
        EventManager.call(event);
        this.pos = pos = event.getPos();
        this.blockPos.set(pos.x, pos.y, pos.z);
        ci.cancel();
    }

    private void applyBabyModelCameraOffset(Entity focusedEntity, boolean thirdPerson) {
        BabyModel babyModel = BabyModel.INSTANCE;
        if (!babyModel.shouldAdjustThirdPersonCamera(focusedEntity, thirdPerson)) {
            return;
        }
        Vec3d start = this.pos;
        Vec3d end = start.subtract(0.0, babyModel.getThirdPersonEyeOffset(), 0.0);
        this.pos = this.clipBabyModelCameraOffset(start, end, focusedEntity);
        this.blockPos.set(this.pos.x, this.pos.y, this.pos.z);
    }

    private Vec3d clipBabyModelCameraOffset(Vec3d start, Vec3d end, Entity focusedEntity) {
        if (mc == null || CameraMixin.mc.world == null || focusedEntity == null) {
            return end;
        }
        BlockHitResult hit = CameraMixin.mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, focusedEntity));
        if (!(hit instanceof BlockHitResult)) {
            return end;
        }
        BlockHitResult blockHit = hit;
        Vec3d direction = end.subtract(start);
        double distance = direction.length();
        if (distance <= 1.0E-6) {
            return start;
        }
        double hitDistance = Math.sqrt(start.squaredDistanceTo(blockHit.getPos()));
        double safeDistance = Math.max(0.0, Math.min(distance, hitDistance - 0.03));
        if (safeDistance <= 1.0E-6) {
            return start;
        }
        return start.add(direction.normalize().multiply(safeDistance));
    }
}

