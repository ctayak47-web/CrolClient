package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.*;
import crol.client.modules.impl.player.NoPush;
import crol.client.modules.impl.render.CustomModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private Box boundingBox;
    @Shadow public double prevX;
    @Shadow public double prevZ;
    @Shadow public boolean horizontalCollision;

    @Unique private Vec3d winnerclient$originalMovement;

    @Shadow public abstract Vec3d getPos();
    @Shadow public abstract Box getBoundingBox();
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();


    @Inject(method = "movementInputToVelocity", at = @At("HEAD"), cancellable = true)
    private static void onMovementInputToVelocity(Vec3d movementInput, float speed, float yaw, CallbackInfoReturnable<Vec3d> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        if (yaw == client.player.getYaw()) {
            double lengthSq = movementInput.lengthSquared();
            if (lengthSq < 1.0E-7) {
                cir.setReturnValue(Vec3d.ZERO);
                return;
            }

            MoveCorrectionEvent moveCorrectionEvent = new MoveCorrectionEvent(yaw, 0.0F);
            CrolClient.INSTANCE.getEventManager().hookEvent(moveCorrectionEvent);
            float correctedYaw = moveCorrectionEvent.getYaw();

            Vec3d normalized = (lengthSq > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
            float sin = MathHelper.sin(correctedYaw * (float)(Math.PI / 180.0));
            float cos = MathHelper.cos(correctedYaw * (float)(Math.PI / 180.0));

            cir.setReturnValue(new Vec3d(
                    normalized.x * cos - normalized.z * sin,
                    normalized.y,
                    normalized.z * cos + normalized.x * sin
            ));
        }
    }

    @Inject(method = "getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void onGetRotationVector(float pitch, float yaw, CallbackInfoReturnable<Vec3d> cir) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            FireworkEvent fireworkEvent = new FireworkEvent(yaw, pitch);
            CrolClient.INSTANCE.getEventManager().hookEvent(fireworkEvent);

            if (fireworkEvent.getPitch() != pitch || fireworkEvent.getYaw() != yaw) {
                float f = (float) (fireworkEvent.getPitch() * (float)(Math.PI / 180.0));
                float g = (float) (-fireworkEvent.getYaw() * (float)(Math.PI / 180.0));
                float h = MathHelper.cos(g);
                float i = MathHelper.sin(g);
                float j = MathHelper.cos(f);
                float k = MathHelper.sin(f);
                cir.setReturnValue(new Vec3d(i * j, -k, h * j));
            }
        }
    }

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    public final void onGetBoundingBox(CallbackInfoReturnable<Box> cir) {
        BoundingBoxControlEvent event = new BoundingBoxControlEvent(this.boundingBox, (Entity) (Object) this);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);
        cir.setReturnValue(event.getBox());
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
        CustomModel customModel = (CustomModel) CrolClient.INSTANCE.getModuleManager().getByClass(CustomModel.class);
        if (customModel != null && customModel.isEnabled() && customModel.getCustomModel() == entity) {
            ci.cancel();
        }
    }

    @ModifyArgs(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void onPushAwayFromHook(Args args) {
        NoPush noPush = (NoPush) CrolClient.INSTANCE.getModuleManager().getByClass(NoPush.class);
        if ((Object) this == MinecraftClient.getInstance().player && noPush != null && noPush.isEnabled() && noPush.options.getValueByName("Players")) {
            args.set(0, 0.0);
            args.set(1, 0.0);
            args.set(2, 0.0);
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void captureMovement(MovementType type, Vec3d movement, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            this.winnerclient$originalMovement = movement;
        }
    }

    @ModifyVariable(
            method = "move",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", shift = At.Shift.AFTER),
            ordinal = 0
    )
    private Vec3d hookMoveEvent(Vec3d adjustedMovement, MovementType type, Vec3d originalMovement) {
        if (!((Object) this instanceof ClientPlayerEntity)) {
            return adjustedMovement;
        }

        Vec3d from = this.getPos();
        Vec3d predictedPosition = from.add(adjustedMovement);
        Vec3d intended = this.winnerclient$originalMovement != null ? this.winnerclient$originalMovement : originalMovement;

        boolean collidedVertically = intended.y != adjustedMovement.y;
        boolean collidedHorizontally = this.horizontalCollision;
        boolean onGround = collidedVertically && intended.y < 0.0;

        MoveOrEvent event = new MoveOrEvent(from, predictedPosition, originalMovement, onGround, collidedHorizontally, collidedVertically, this.getBoundingBox());
        CrolClient.INSTANCE.getEventManager().hookEvent(event);

        // ИСПРАВЛЕНИЕ: Конвертируем Vector3d (твой) в Vec3d (Minecraft)
        return new Vec3d(event.getMotion().x, event.getMotion().y, event.getMotion().z);
    }

    @Inject(method = "move", at = @At("TAIL"))
    private void hookPostMoveEvent(MovementType type, Vec3d movement, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            double deltaX = this.getX() - this.prevX;
            double deltaZ = this.getZ() - this.prevZ;
            double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            CrolClient.INSTANCE.getEventManager().hookEvent(new PostMoveEvent(dist));
        }
    }
}