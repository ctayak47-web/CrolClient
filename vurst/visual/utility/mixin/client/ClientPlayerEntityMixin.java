
package vurst.visual.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.MovementType;
import net.minecraft.Vec3d;
import net.minecraft.MinecraftClient;
import net.minecraft.ClientWorld;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vurst.visual.base.events.impl.other.EventCloseScreen;
import vurst.visual.base.events.impl.player.EventMove;
import vurst.visual.base.events.impl.player.EventSlowWalking;
import vurst.visual.base.events.impl.player.EventSprintUpdate;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.impl.utility.LockSlot;

@Mixin(value={ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin
extends AbstractClientPlayerEntity {
    @Shadow
    private float lastYaw;
    @Shadow
    @Final
    protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    protected abstract void sendSprintingPacket();

    @Shadow
    protected abstract void autoJump(float var1, float var2);

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    public void tick(CallbackInfo ci) {
        EventManager.call(new EventUpdate());
    }

    @Inject(method={"dropSelectedItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (LockSlot.INSTANCE.shouldBlockHotbarDrop(this.getInventory().selectedSlot, this.getInventory().getStack(this.getInventory().selectedSlot))) {
            cir.setReturnValue((Object)false);
        }
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;sendSprintingPacket()V"))
    public void invokeSprintUpdate(ClientPlayerEntity instance) {
        EventSprintUpdate eventSprintUpdate = new EventSprintUpdate();
        EventManager.call(eventSprintUpdate);
        if (!eventSprintUpdate.isCancelled()) {
            this.sendSprintingPacket();
        }
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    public float replaceMovePacketYaw(ClientPlayerEntity instance) {
        return instance.getYaw();
    }

    @Redirect(method={"tickMovement"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require=0)
    private boolean onIsUsingItemRedirect(ClientPlayerEntity player) {
        if (player.isUsingItem()) {
            EventSlowWalking slowDownEvent = new EventSlowWalking();
            EventManager.call(slowDownEvent);
            return player.isUsingItem() && player.getVehicle() == null && !slowDownEvent.isCancelled();
        }
        return player.isUsingItem() && player.getVehicle() == null;
    }

    @Redirect(method={"sendMovementPackets"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    public float replaceMovePacketPitch(ClientPlayerEntity instance) {
        return instance.getPitch();
    }

    @Inject(method={"closeHandledScreen"}, at={@At(value="HEAD")}, cancellable=true)
    private void closeHandledScreenHook(CallbackInfo info) {
        EventCloseScreen event = new EventCloseScreen(this.client.currentScreen);
        EventManager.call(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V")}, cancellable=true)
    public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        EventMove event = new EventMove(movement);
        EventManager.call(event);
        double d = this.getX();
        double e = this.getZ();
        super.move(movementType, event.getMovePos());
        this.autoJump((float)(this.getX() - d), (float)(this.getZ() - e));
        ci.cancel();
    }
}

