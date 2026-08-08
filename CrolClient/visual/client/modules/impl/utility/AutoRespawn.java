
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.DeathScreen;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.StringSetting;

@ModuleAnnotation(name="Auto Respawn", category=Category.MOVEMENT, description="Автоматически возрождает после смерти.")
public final class AutoRespawn
extends Module {
    public static final AutoRespawn INSTANCE = new AutoRespawn();
    private final BooleanSetting sendCommandAfterRespawn = new BooleanSetting("Команда после респавна", false);
    private final StringSetting command = new StringSetting("Команда", "/spawn", 100, this.sendCommandAfterRespawn::isEnabled);
    private boolean respawnRequested;
    private boolean pendingCommand;
    private int postRespawnDelayTicks;

    private AutoRespawn() {
    }

    @Override
    public void onDisable() {
        this.respawnRequested = false;
        this.pendingCommand = false;
        this.postRespawnDelayTicks = 0;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        boolean playerDead;
        if (AutoRespawn.mc.player == null || AutoRespawn.mc.world == null) {
            return;
        }
        boolean deathScreenOpen = AutoRespawn.mc.currentScreen instanceof DeathScreen;
        boolean bl = playerDead = !AutoRespawn.mc.player.isAlive() || AutoRespawn.mc.player.getHealth() <= 0.0f;
        if (deathScreenOpen && AutoRespawn.mc.player.deathTime > 5 && !this.respawnRequested) {
            AutoRespawn.mc.player.requestRespawn();
            this.respawnRequested = true;
            if (this.sendCommandAfterRespawn.isEnabled()) {
                this.pendingCommand = true;
                this.postRespawnDelayTicks = 4;
            }
            return;
        }
        if (playerDead) {
            return;
        }
        if (this.respawnRequested) {
            this.respawnRequested = false;
        }
        if (!this.pendingCommand || deathScreenOpen) {
            return;
        }
        if (this.postRespawnDelayTicks > 0) {
            --this.postRespawnDelayTicks;
            return;
        }
        this.sendConfiguredCommand();
        this.pendingCommand = false;
    }

    private void sendConfiguredCommand() {
        if (AutoRespawn.mc.player == null || AutoRespawn.mc.player.networkHandler == null) {
            return;
        }
        String raw = this.command.getValue();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String normalized = raw.trim();
        if (normalized.isBlank() || normalized.equals("/")) {
            return;
        }
        AutoRespawn.mc.player.networkHandler.sendChatMessage(normalized);
    }
}

