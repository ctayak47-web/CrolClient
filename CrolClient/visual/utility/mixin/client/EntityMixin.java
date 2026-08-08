
package crol.client.utility.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.Entity;
import net.minecraft.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import crol.client.CrolClient;
import crol.client.base.rotation.RotationManager;
import crol.client.modules.impl.render.NameF5;
import crol.client.modules.impl.render.NoRender;
import crol.client.utility.interfaces.IMinecraft;

@Mixin(value={Entity.class})
public class EntityMixin
implements IMinecraft {
    @ModifyExpressionValue(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isControlledByPlayer()Z")})
    public boolean fixFalldistanceValue(boolean original) {
        if (this == EntityMixin.mc.player) {
            return false;
        }
        return original;
    }

    @Redirect(method={"updateVelocity"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;getYaw()F"))
    public float movementCorrection(Entity instance) {
        RotationManager rotationManager;
        if (instance instanceof ClientPlayerEntity && !(rotationManager = CrolClient.getInstance().getRotationManager()).isSetRotation()) {
            return rotationManager.getCurrentRotation().getYaw();
        }
        return instance.getYaw();
    }

    @ModifyVariable(method={"getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private float modifyPitch(float pitch) {
        if (this instanceof ClientPlayerEntity) {
            RotationManager rotationManager = CrolClient.getInstance().getRotationManager();
            if (rotationManager.isSetRotation()) {
                return pitch;
            }
            return rotationManager.getCurrentRotation().getPitch();
        }
        return pitch;
    }

    @ModifyVariable(method={"getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;"}, at=@At(value="HEAD"), ordinal=1, argsOnly=true)
    private float modifyYaw(float yaw) {
        if (this instanceof ClientPlayerEntity) {
            RotationManager rotationManager = CrolClient.getInstance().getRotationManager();
            if (rotationManager.isSetRotation()) {
                return yaw;
            }
            return rotationManager.getCurrentRotation().getYaw();
        }
        return yaw;
    }

    @Inject(method={"isGlowing"}, at={@At(value="HEAD")}, cancellable=true)
    private void disableGlow(CallbackInfoReturnable<Boolean> cir) {
        if (NoRender.INSTANCE.isDisableGlow()) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"shouldRenderName"}, at={@At(value="HEAD")}, cancellable=true)
    private void forceRenderName(CallbackInfoReturnable<Boolean> cir) {
        if (this == EntityMixin.mc.player && NameF5.INSTANCE.shouldShowName()) {
            cir.setReturnValue((Object)true);
        }
    }
}

