package ru.crolclient.implement.features.modules.combat.killaura.attack;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.common.QuickImports;
import ru.crolclient.core.Extra;
import ru.crolclient.core.listener.impl.PacketEventListener;
import ru.crolclient.implement.features.modules.movement.AutoSprintModule;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SprintManager implements QuickImports {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    @Setter
    Mode currentMode;
    boolean isStopSprintPacketSent;

    public void tick(AttackHandler parent) {
        if (currentMode == Mode.DYNAMIC ) {
            Module autoSprintModule = Extra.getInstance().getModuleProvider().module("AutoSprint");
            if (autoSprintModule instanceof AutoSprintModule autoSprint && autoSprintModule.isState()) {
                autoSprint.setEmergencyStop(true);
            }
        }
    }

    public void preAttack() {
        if (currentMode == Mode.LEGACY) {
            if (PacketEventListener.serverSprint && mc.player.lastSprinting) {
                mc.options.sprintKey.setPressed(false);
                //mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                isStopSprintPacketSent = true;
            }
        } else if (currentMode == Mode.LEGIT) {
            if (!PacketEventListener.serverSprint && mc.player.lastSprinting) {
                mc.options.sprintKey.setPressed(true);
            }
        }
    }

    public void postAttack() {
        if (currentMode == Mode.LEGACY) {
            if (isStopSprintPacketSent && (!PacketEventListener.serverSprint && mc.player.lastSprinting)) {
                mc.options.sprintKey.setPressed(true);
                //mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                isStopSprintPacketSent = false;
            }
        } else if (currentMode == Mode.LEGIT) {
            if (!mc.player.isSprinting() && mc.player.lastSprinting) {
                mc.options.sprintKey.setPressed(true);
            }
        }
    }

    public enum Mode {
        NONE, DYNAMIC, LEGACY, LEGIT
    }
}
