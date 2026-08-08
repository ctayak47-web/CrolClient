package crol.client.mixins.hooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import crol.client.CrolClient;
import crol.client.event.EventType;
import crol.client.event.classes.MoveEvent;
import crol.client.event.classes.RotationEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.classes.UsingItemEvent;
import crol.client.modules.impl.player.NoPush;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerMixin {

    @Shadow private boolean lastSprinting;
    @Shadow private double lastX;
    @Shadow private double lastBaseY;
    @Shadow private double lastZ;
    @Shadow private float lastYaw;
    @Shadow private float lastPitch;
    @Shadow private boolean lastOnGround;
    @Final
    @Shadow public ClientPlayNetworkHandler networkHandler;
    @Shadow private boolean lastHorizontalCollision;
    @Shadow private boolean autoJumpEnabled;
    @Shadow private int ticksSinceLastPositionPacketSent;
    @Unique private double wild$prevX, wild$prevZ;
    @Unique private float wild$prevBodyYaw;
    @Unique private float wild$prevSpoofedPitch;
    @Shadow protected abstract void sendSprintingPacket();
    @Shadow protected abstract boolean isCamera();
    @Shadow @Final protected MinecraftClient client;

    @Shadow
    protected abstract void autoJump(float dx, float dz);

    @Redirect(
            method = "tickMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
            ),
            require = 0
    )
    private boolean onIsUsingItemRedirect(ClientPlayerEntity player) {
        if (player.isUsingItem()) {
            UsingItemEvent event = new UsingItemEvent(EventType.ON);
            CrolClient.INSTANCE.getEventManager().hookEvent(event);

            if (event.isCancelled()) {
                return false;
            }
        }

        return player.isUsingItem() && player.getVehicle() == null;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo callbackInfo){
        if (client.player != null && client.world != null) {
            TickEvent tickEvent = new TickEvent();
            CrolClient.INSTANCE.getEventManager().hookEvent(tickEvent);
        }
    }


    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocksHook(double x, double z, CallbackInfo info) {
        NoPush noPush = (NoPush) CrolClient.INSTANCE.getModuleManager().getByClass(NoPush.class);
        if (noPush != null && noPush.isEnabled() && noPush.options.getValueByName("Blocks")) {
            info.cancel();
        }
    }
    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPackets(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        boolean sprinting = player.isSprinting();
        if (sprinting != this.lastSprinting) {
            ClientCommandC2SPacket.Mode mode = sprinting ?
                    ClientCommandC2SPacket.Mode.START_SPRINTING :
                    ClientCommandC2SPacket.Mode.STOP_SPRINTING;

            this.networkHandler.sendPacket(new ClientCommandC2SPacket(player, mode));
            this.lastSprinting = sprinting;
        }

        if (isCamera()) {
            RotationEvent rotationEvent = new RotationEvent(player.getPitch(), player.getYaw());
            CrolClient.INSTANCE.getEventManager().hookEvent(rotationEvent);

            float currentYaw = rotationEvent.getYaw();
            float currentPitch = rotationEvent.getPitch();

            double dX = player.getX() - this.lastX;
            double dY = player.getY() - this.lastBaseY;
            double dZ = player.getZ() - this.lastZ;
            double dYaw = currentYaw - this.lastYaw;
            double dPitch = currentPitch - this.lastPitch;

            this.ticksSinceLastPositionPacketSent++;

            boolean moved = MathHelper.squaredMagnitude(dX, dY, dZ) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean rotated = dYaw != 0.0 || dPitch != 0.0;
            boolean onGround = player.isOnGround();
            boolean collision = player.horizontalCollision;

            if (moved && rotated) {
                networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), currentYaw, currentPitch, onGround, collision));
            } else if (moved) {
                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX(), player.getY(), player.getZ(), onGround, collision));
            } else if (rotated) {
                networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(currentYaw, currentPitch, onGround, collision));
            } else if (this.lastOnGround != onGround || this.lastHorizontalCollision != collision) {
                networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(onGround, collision));
            }

            if (moved) {
                this.lastX = player.getX();
                this.lastBaseY = player.getY();
                this.lastZ = player.getZ();
                this.ticksSinceLastPositionPacketSent = 0;
            }

            if (rotated) {
                this.lastYaw = currentYaw;
                this.lastPitch = currentPitch;
            }

            this.lastOnGround = onGround;
            this.lastHorizontalCollision = collision;
            this.autoJumpEnabled = client.options.getAutoJump().getValue();

            player.setBodyYaw(calculateProperBodyYaw(currentYaw));
            player.setHeadYaw(currentYaw);
            player.renderPitch = currentPitch;
        }

        ci.cancel();
    }
    @Unique
    private float calculateProperBodyYaw(float yaw) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        double dx = player.getX() - wild$prevX;
        double dz = player.getZ() - wild$prevZ;
        float motionSq = (float) (dx * dx + dz * dz);
        float bodyYaw = wild$prevBodyYaw;

        if (motionSq > 0.0025f) {
            float moveYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
            float diff = Math.abs(MathHelper.wrapDegrees(yaw) - moveYaw);
            bodyYaw = (diff > 95.0f && diff < 265.0f) ? moveYaw - 180.0f : moveYaw;
        }

        bodyYaw = wild$prevBodyYaw + MathHelper.wrapDegrees(bodyYaw - wild$prevBodyYaw) * 0.3f;
        float offset = MathHelper.wrapDegrees(yaw - bodyYaw);

        if (Math.abs(offset) > 52.0f) {
            bodyYaw += offset - (MathHelper.sign(offset) * 52.0f);
        }

        wild$prevX = player.getX();
        wild$prevZ = player.getZ();
        wild$prevBodyYaw = bodyYaw;

        return bodyYaw;
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void onMoveHook(MovementType type, Vec3d movement, CallbackInfo ci) {
        MoveEvent event = new MoveEvent(movement);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);
        Vec3d modifiedMovement = event.getMovement();

        if (!modifiedMovement.equals(movement)) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            double startX = player.getX();
            double startZ = player.getZ();

            ((Entity) (Object) this).move(type, modifiedMovement);

            float diffX = (float) (player.getX() - startX);
            float diffZ = (float) (player.getZ() - startZ);
            this.autoJump(diffX, diffZ);
            player.distanceMoved += MathHelper.hypot(diffX, diffZ) * 0.6F;

            ci.cancel();
        }
    }
}