
package crol.client.utility.mixin.minecraft.entity;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.CrolClient;
import crol.client.base.events.impl.player.EventJump;
import crol.client.base.rotation.RotationManager;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.render.entity.EntityDamageTracker;

@Mixin(value={LivingEntity.class})
public class LivingEntityMixin
implements IMinecraft {
    @Shadow
    public int hurtTime;
    @Unique
    private int CrolClient$hurtTimeBeforeStatus;

    @Redirect(method={"jump"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    public float replaceMovePacketPitch(LivingEntity instance) {
        RotationManager rotationManager;
        if (this == LivingEntityMixin.mc.player && !(rotationManager = CrolClient.getInstance().getRotationManager()).isSetRotation()) {
            return rotationManager.getCurrentRotation().getYaw();
        }
        return instance.getYaw();
    }

    @Inject(method={"jump"}, at={@At(value="HEAD")})
    private void onJump(CallbackInfo info) {
        if (this == LivingEntityMixin.mc.player) {
            EventManager.call(new EventJump());
        }
    }

    @Inject(method={"handleStatus(B)V"}, at={@At(value="HEAD")})
    private void CrolClient$captureHurtTimeBeforeStatus(byte status, CallbackInfo ci) {
        this.CrolClient$hurtTimeBeforeStatus = this.hurtTime;
    }

    @Inject(method={"handleStatus(B)V"}, at={@At(value="RETURN")})
    private void CrolClient$trackDamageFromHurtAnimation(byte status, CallbackInfo ci) {
        if (this.hurtTime > 0 && this.hurtTime > this.CrolClient$hurtTimeBeforeStatus) {
            EntityDamageTracker.markDamaged((LivingEntity)this);
        }
    }
}

