
package vurst.visual.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.Hand;
import net.minecraft.StatusEffects;
import net.minecraft.PlayerEntity;
import net.minecraft.Vec3d;
import net.minecraft.Packet;
import net.minecraft.PlayerInteractEntityC2SPacket;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.server.EventPacket;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;

@ModuleAnnotation(name="ShiftTap", category=Category.MOVEMENT, description="Коротко прожимает шифт после крит-атаки.")
public final class ShiftTap
extends Module {
    public static final ShiftTap INSTANCE = new ShiftTap();
    private final NumberSetting holdMs = new NumberSetting("Удержание (мс)", 180.0f, 40.0f, 500.0f, 5.0f);
    private boolean sneaking;
    private long sneakEndTime;

    private ShiftTap() {
    }

    @Override
    public void onDisable() {
        this.sneaking = false;
        this.restoreSneakKeyState();
        super.onDisable();
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (ShiftTap.mc.player == null || ShiftTap.mc.world == null || !event.isSent()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof PlayerInteractEntityC2SPacket)) {
            return;
        }
        PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket)packet;
        if (!this.isAttackPacket(packet) || !this.canCrit((PlayerEntity)ShiftTap.mc.player)) {
            return;
        }
        this.sneaking = true;
        this.sneakEndTime = System.currentTimeMillis() + (long)Math.round(this.holdMs.getCurrent());
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (ShiftTap.mc.player == null || ShiftTap.mc.options == null || !this.sneaking) {
            return;
        }
        ShiftTap.mc.options.sneakKey.setPressed(true);
        if (System.currentTimeMillis() > this.sneakEndTime) {
            this.sneaking = false;
            this.restoreSneakKeyState();
        }
    }

    private boolean isAttackPacket(PlayerInteractEntityC2SPacket packet) {
        final boolean[] attack = new boolean[]{false};
        packet.handle(new PlayerInteractEntityC2SPacket.Handler(){

            public void attack() {
                attack[0] = true;
            }

            public void interact(Hand hand) {
            }

            public void interactAt(Hand hand, Vec3d pos) {
            }
        });
        return attack[0];
    }

    private boolean canCrit(PlayerEntity player) {
        return player.fallDistance > 0.0f && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater() && !player.isSubmergedInWater() && !player.hasStatusEffect(StatusEffects.BLINDNESS) && !player.hasVehicle();
    }

    private void restoreSneakKeyState() {
        if (ShiftTap.mc.options == null) {
            return;
        }
        ShiftTap.mc.options.sneakKey.setPressed(PlayerIntersectionUtil.isKey(ShiftTap.mc.options.sneakKey.getDefaultKey()));
    }
}

