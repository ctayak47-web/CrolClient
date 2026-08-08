
package crol.client.base.rotation;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.PlayerRotationS2CPaket;
import net.minecraft.Entity;
import net.minecraft.Packet;
import net.minecraft.PlayerPositionLookS2CPacket;
import net.minecraft.PlayerInteractItemC2SPacket;
import net.minecraft.MathHelper;
import net.minecraft.ClientPlayerEntity;
import crol.client.base.events.impl.other.EventSpawnEntity;
import crol.client.base.events.impl.player.EventAttack;
import crol.client.base.events.impl.player.EventDirection;
import crol.client.base.events.impl.player.EventRotate;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.server.EventPacket;
import crol.client.base.request.RequestHandler;
import crol.client.base.rotation.AimManager;
import crol.client.base.rotation.RotationTarget;
import crol.client.modules.api.Module;
import crol.client.utility.game.player.rotation.Rotation;
import crol.client.utility.interfaces.IMinecraft;

public class RotationManager
implements IMinecraft {
    private Rotation currentRotation = new Rotation(0.0f, 0.0f);
    private Rotation previousRotation = new Rotation(0.0f, 0.0f);
    private final RequestHandler<RotationTarget> requestHandler = new RequestHandler();
    private final AimManager aimManager = new AimManager();
    private RotationTarget previousRotationTarget = new RotationTarget(this.currentRotation, () -> this.currentRotation, this.aimManager.getInstantSetup());
    private boolean setRotation = true;

    public RotationManager() {
        EventManager.register(this);
    }

    @EventTarget
    public void addLocalPlayer(EventSpawnEntity eventSpawnLocalPlayer) {
        Entity entity = eventSpawnLocalPlayer.getEntity();
        if (entity instanceof ClientPlayerEntity) {
            ClientPlayerEntity player = (ClientPlayerEntity)entity;
            this.currentRotation = new Rotation(player.getYaw(), player.getPitch());
            this.previousRotation = new Rotation(player.getYaw(), player.getPitch());
            this.previousRotationTarget = new RotationTarget(this.currentRotation, () -> this.currentRotation, this.aimManager.getInstantSetup());
            this.setRotation = true;
        }
    }

    @EventTarget(value=3)
    public void update(EventUpdate event) {
        EventManager.call(new EventRotate());
        RotationTarget targetRotation = this.requestHandler.getActiveRequestValue();
        if (targetRotation != null) {
            Rotation newRot = targetRotation.rotation().get();
            this.previousRotation = this.currentRotation;
            this.currentRotation = newRot;
            this.setRotation = false;
            this.previousRotationTarget = targetRotation;
        } else if (this.setRotation) {
            this.previousRotation = this.currentRotation;
            this.currentRotation = new Rotation(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch(), true);
        } else {
            Rotation back = new Rotation(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch());
            if (this.currentRotation.rotationDeltaTo(back).isInRange(5.0f)) {
                this.previousRotation = this.currentRotation;
                this.currentRotation = this.aimManager.rotate(this.aimManager.getInstantSetup(), back);
                this.setRotation = true;
            } else {
                Rotation newRot = this.aimManager.rotate(this.previousRotationTarget.rotationConfigBack(), back);
                this.previousRotation = this.currentRotation;
                this.currentRotation = newRot;
            }
        }
        if (!this.setRotation) {
            float delta = this.currentRotation.getYaw() - RotationManager.mc.player.lastYaw;
            Rotation validing = new Rotation(this.currentRotation.getYaw(), this.currentRotation.getPitch());
            if (delta > 320.0f) {
                validing = new Rotation(RotationManager.mc.player.lastYaw + 300.0f, this.currentRotation.getPitch()).normalize(new Rotation(RotationManager.mc.player.lastYaw, RotationManager.mc.player.lastPitch));
            }
            if (delta < -320.0f) {
                validing = new Rotation(RotationManager.mc.player.lastYaw - 300.0f, this.currentRotation.getPitch()).normalize(new Rotation(RotationManager.mc.player.lastYaw, RotationManager.mc.player.lastPitch));
            }
            this.currentRotation = validing;
        }
        this.currentRotation = new Rotation(this.currentRotation.getYaw(), MathHelper.clamp((float)this.currentRotation.getPitch(), (float)-90.0f, (float)90.0f));
        this.requestHandler.tick();
    }

    public void setRotation(RotationTarget targetRotation, int priority, Module module) {
        this.requestHandler.request(new RequestHandler.Request<RotationTarget>(2, priority, module, targetRotation));
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (event.getAction() != EventAttack.Action.PRE || RotationManager.mc.player == null) {
            return;
        }
        if (this.requestHandler.getActiveRequestValue() != null) {
            return;
        }
        Rotation snap = new Rotation(RotationManager.mc.player.getYaw(), RotationManager.mc.player.getPitch());
        if (!this.currentRotation.equals(snap)) {
            this.previousRotation = this.currentRotation;
            this.currentRotation = snap;
            this.previousRotationTarget = new RotationTarget(this.currentRotation, () -> this.currentRotation, this.aimManager.getInstantSetup());
            this.setRotation = true;
        }
    }

    @EventTarget
    public void direction(EventDirection direction) {
        direction.setYaw(this.currentRotation.getYaw());
        direction.setPitch(this.currentRotation.getPitch());
    }

    @EventTarget
    public void packet(EventPacket eventPacket) {
        Packet<?> packet = eventPacket.getPacket();
        Objects.requireNonNull(packet);
        Packet<?> packet = packet;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PlayerRotationS2CPaket.class, PlayerPositionLookS2CPacket.class, PlayerInteractItemC2SPacket.class}, packet, n)) {
            case 0: {
                PlayerRotationS2CPaket player = (PlayerRotationS2CPaket)packet;
                this.currentRotation = new Rotation(player.comp_3231(), player.comp_3230());
                this.previousRotationTarget = new RotationTarget(this.currentRotation, () -> this.currentRotation, this.aimManager.getInstantSetup());
                this.setRotation = true;
                break;
            }
            case 1: {
                PlayerPositionLookS2CPacket player = (PlayerPositionLookS2CPacket)packet;
                this.currentRotation = new Rotation(player.comp_3228().comp_3150(), player.comp_3228().comp_3151());
                this.previousRotationTarget = new RotationTarget(this.currentRotation, () -> this.currentRotation, this.aimManager.getInstantSetup());
                this.setRotation = true;
                break;
            }
            case 2: {
                PlayerInteractItemC2SPacket packetItem = (PlayerInteractItemC2SPacket)packet;
                packetItem.yaw = this.currentRotation.getYaw();
                packetItem.pitch = this.currentRotation.getPitch();
                break;
            }
        }
    }

    @Generated
    public Rotation getCurrentRotation() {
        return this.currentRotation;
    }

    @Generated
    public Rotation getPreviousRotation() {
        return this.previousRotation;
    }

    @Generated
    public RequestHandler<RotationTarget> getRequestHandler() {
        return this.requestHandler;
    }

    @Generated
    public AimManager getAimManager() {
        return this.aimManager;
    }

    @Generated
    public RotationTarget getPreviousRotationTarget() {
        return this.previousRotationTarget;
    }

    @Generated
    public boolean isSetRotation() {
        return this.setRotation;
    }
}

