package ru.crolclient.mixins;

import ru.crolclient.implement.features.modules.misc.ZeroHitboxModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.events.player.BoundingBoxControlEvent;
import ru.crolclient.implement.events.player.PlayerVelocityStrafeEvent;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        return null;
    }

    @Shadow
    private Box boundingBox;

    @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d hookVelocity(Vec3d movementInput, float speed, float yaw) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            PlayerVelocityStrafeEvent event = new PlayerVelocityStrafeEvent(movementInput, speed, yaw, EntityMixin.movementInputToVelocity(movementInput, speed, yaw));
            EventManager.callEvent(event);
            return event.getVelocity();
        }

        return EntityMixin.movementInputToVelocity(movementInput, speed, yaw);
    }

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
        Entity entity = (Entity) (Object) this;
        BoundingBoxControlEvent event = new BoundingBoxControlEvent(boundingBox, entity);
        EventManager.callEvent(event);

        if (event.isCancelled()) {
            cir.setReturnValue(event.getChangedBox());
            return;
        }

        ZeroHitboxModule zeroHitbox = (ZeroHitboxModule) Extra.getInstance().getModuleProvider().module("ZeroHitbox");
        if (zeroHitbox == null || !zeroHitbox.isState() || entity == MinecraftClient.getInstance().player) return;

        boolean shouldZero = (entity instanceof PlayerEntity && zeroHitbox.getTargetSettings().isSelected("Players"))
                || (entity instanceof MobEntity && zeroHitbox.getTargetSettings().isSelected("Mobs"))
                || (entity instanceof ArmorStandEntity && zeroHitbox.getTargetSettings().isSelected("Armor Stands"));

        if (shouldZero) {
            cir.setReturnValue(new Box(
                    boundingBox.minX,
                    boundingBox.minY,
                    boundingBox.minZ,
                    boundingBox.minX,
                    boundingBox.maxY,
                    boundingBox.minZ
            ));
        }
    }
}
