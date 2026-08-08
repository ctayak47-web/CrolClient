package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.EntityDeathEvent;
import crol.client.event.classes.MoveCorrectionEvent;
import crol.client.event.classes.TravelEvent;
import crol.client.mixins.other.ILivingEntityMixin;
import crol.client.modules.impl.render.SwingAnimation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntityMixin {

    @Shadow private int jumpingCooldown;

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void getHandSwingDurationHook(CallbackInfoReturnable<Integer> info) {
        SwingAnimation swingAnimation = (SwingAnimation) CrolClient.INSTANCE.getModuleManager().getByClass(SwingAnimation.class);
        if (swingAnimation != null && swingAnimation.isEnabled()) {
            info.setReturnValue((int) swingAnimation.slow.getValue());
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Логика работает только для игрока
        if (entity instanceof ClientPlayerEntity) {
            float jumpVelocity = this.getJumpVelocityCustom();
            if (jumpVelocity > 1.0E-5F) {
                Vec3d velocity = entity.getVelocity();
                entity.setVelocity(velocity.x, Math.max((double) jumpVelocity, velocity.y), velocity.z);

                if (entity.isSprinting()) {
                    MoveCorrectionEvent moveCorrectionEvent = new MoveCorrectionEvent(entity.getYaw(), 0.0F);
                    CrolClient.INSTANCE.getEventManager().hookEvent(moveCorrectionEvent);

                    float yaw = moveCorrectionEvent.getYaw();
                    float radians = yaw * (float) (Math.PI / 180.0);
                    entity.addVelocity((double) (-MathHelper.sin(radians)) * 0.2, 0.0, (double) MathHelper.cos(radians) * 0.2);
                }
                entity.velocityDirty = true;
            }
            ci.cancel();
        }
    }

    @Unique
    private float getJumpVelocityCustom() {
        LivingEntity entity = (LivingEntity) (Object) this;
        float strength = 1.0F;
        float baseJumpStrength = (float) entity.getAttributeValue(EntityAttributes.JUMP_STRENGTH);
        return baseJumpStrength * strength * this.getCustomJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    @Unique
    private float getJumpBoostVelocityModifier() {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            return 0.1F * ((float) Objects.requireNonNull(entity.getStatusEffect(StatusEffects.JUMP_BOOST)).getAmplifier() + 1.0F);
        }
        return 0.0F;
    }

    @Unique
    private float getCustomJumpVelocityMultiplier() {
        LivingEntity entity = (LivingEntity) (Object) this;
        float f = entity.getWorld().getBlockState(entity.getBlockPos()).getBlock().getJumpVelocityMultiplier();
        float g = entity.getWorld().getBlockState(entity.getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
        return (double) f == 1.0 ? g : f;
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        EntityDeathEvent event = new EntityDeathEvent((LivingEntity) (Object) this, source);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);
    }

    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void handleStatus(byte status, CallbackInfo ci) {
        if (status == 3) {
            EntityDeathEvent event = new EntityDeathEvent((LivingEntity) (Object) this, null);
            CrolClient.INSTANCE.getEventManager().hookEvent(event);
        }
    }

    @Inject(method = "calcGlidingVelocity", at = @At("HEAD"), cancellable = true)
    private void onApplyElytraFlight(Vec3d oldVelocity, CallbackInfoReturnable<Vec3d> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (entity == client.player) {
            Vec3d rotationVec = entity.getRotationVector();

            TravelEvent travelEvent = new TravelEvent(entity.getYaw(), entity.getPitch());
            CrolClient.INSTANCE.getEventManager().hookEvent(travelEvent);

            float pitchRadians = (float) (travelEvent.getPitch() * (Math.PI / 180.0));
            double horizontalDist = Math.sqrt(rotationVec.x * rotationVec.x + rotationVec.z * rotationVec.z);
            double currentSpeed = oldVelocity.horizontalLength();
            double gravity = this.getEffectiveGravityCustom();
            double cosPitch = MathHelper.clamp(Math.cos(pitchRadians), 0.0, 1.0);

            Vec3d newVelocity = oldVelocity.add(0.0, gravity * (-1.0 + cosPitch * 0.75), 0.0);

            if (newVelocity.y < 0.0 && horizontalDist > 0.0) {
                double boost = newVelocity.y * -0.1 * cosPitch;
                newVelocity = newVelocity.add(rotationVec.x * boost / horizontalDist, boost, rotationVec.z * boost / horizontalDist);
            }

            if (pitchRadians < 0.0F && horizontalDist > 0.0) {
                double speedBoost = currentSpeed * (double) (-MathHelper.sin(pitchRadians)) * 0.04;
                newVelocity = newVelocity.add(-rotationVec.x * speedBoost / horizontalDist, speedBoost * 3.2, -rotationVec.z * speedBoost / horizontalDist);
            }

            if (horizontalDist > 0.0) {
                newVelocity = newVelocity.add((rotationVec.x / horizontalDist * currentSpeed - newVelocity.x) * 0.1, 0.0, (rotationVec.z / horizontalDist * currentSpeed - newVelocity.z) * 0.1);
            }

            cir.setReturnValue(newVelocity.multiply(0.99, 0.98, 0.99));
        }
    }

    @Unique
    private double getEffectiveGravityCustom() {
        LivingEntity entity = (LivingEntity) (Object) this;
        boolean falling = entity.getVelocity().y <= 0.0;
        double gravity = entity.getFinalGravity();
        if (falling && entity.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return Math.min(gravity, 0.01);
        }
        return gravity;
    }

    public void setJumpingCooldown(int value) {
        this.jumpingCooldown = value;
    }
}