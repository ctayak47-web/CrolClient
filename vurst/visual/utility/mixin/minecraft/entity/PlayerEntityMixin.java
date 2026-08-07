
package vurst.visual.utility.mixin.minecraft.entity;

import net.minecraft.PlayerEntity;
import net.minecraft.World;
import net.minecraft.SoundEvent;
import net.minecraft.SoundEvents;
import net.minecraft.SoundCategory;
import net.minecraft.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.VurstVisual;
import vurst.visual.base.rotation.RotationManager;
import vurst.visual.client.modules.impl.utility.HitSound;
import vurst.visual.utility.game.player.rotation.Rotation;
import vurst.visual.utility.interfaces.IMinecraft;

@Mixin(value={PlayerEntity.class})
public class PlayerEntityMixin {
    @Mutable
    float savedYaw;
    @Mutable
    float savedPitch;
    boolean appliedRotationOverride;

    @Inject(method={"tickMovement"}, at={@At(value="HEAD")})
    private void tickMovement(CallbackInfo ci) {
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")})
    public void fixElytra(CallbackInfo ci) {
        PlayerEntityMixin playerEntityMixin = this;
        if (playerEntityMixin instanceof ClientPlayerEntity) {
            ClientPlayerEntity player = (ClientPlayerEntity)playerEntityMixin;
            RotationManager rotationManager = VurstVisual.getInstance().getRotationManager();
            if (!rotationManager.isSetRotation()) {
                Rotation currentRotation = rotationManager.getCurrentRotation();
                this.savedYaw = IMinecraft.mc.player.getYaw();
                this.savedPitch = IMinecraft.mc.player.getPitch();
                player.setYaw(currentRotation.getYaw());
                player.setPitch(currentRotation.getPitch());
                this.appliedRotationOverride = true;
            }
        }
    }

    @Inject(method={"travel"}, at={@At(value="RETURN")})
    public void fixElytraEnd(CallbackInfo ci) {
        PlayerEntityMixin playerEntityMixin = this;
        if (playerEntityMixin instanceof ClientPlayerEntity) {
            ClientPlayerEntity player = (ClientPlayerEntity)playerEntityMixin;
            if (this.appliedRotationOverride) {
                player.setYaw(this.savedYaw);
                player.setPitch(this.savedPitch);
                this.appliedRotationOverride = false;
            }
        }
    }

    @Redirect(method={"attack"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;)V"))
    private void suppressAttackWorldSound(World world, PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category) {
        if (this == IMinecraft.mc.player && except == IMinecraft.mc.player && HitSound.INSTANCE.shouldSuppressDefaultHits() && this.isAttackSound(sound)) {
            HitSound.INSTANCE.captureSuppressedAttackSound(sound);
            return;
        }
        world.playSound(except, x, y, z, sound, category);
    }

    @Redirect(method={"attack"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private void suppressAttackWorldSound(World world, PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (this == IMinecraft.mc.player && except == IMinecraft.mc.player && HitSound.INSTANCE.shouldSuppressDefaultHits() && this.isAttackSound(sound)) {
            HitSound.INSTANCE.captureSuppressedAttackSound(sound);
            return;
        }
        world.playSound(except, x, y, z, sound, category, volume, pitch);
    }

    private boolean isAttackSound(SoundEvent sound) {
        return sound == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG || sound == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK || sound == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT || sound == SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP || sound == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK || sound == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE;
    }
}

