package ru.crolclient.implement.features.modules.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.implement.events.packet.PacketEvent;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.common.util.math.Counter;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServerRPSpooferModule extends Module {
    @NonFinal
    @Getter
    @Setter
    ResourcePackAction currentAction = ResourcePackAction.WAIT;
    @Getter
    Counter counter = Counter.create();
    @Setter
    @NonFinal
    private MinecraftClient mc;
    public ServerRPSpooferModule() {
        super("ServerRPSpoof", "Server RP Spoof", ModuleCategory.MISC);
    }

    @EventHandler
    public void onPacket(PacketEvent packetEvent) {
        if (packetEvent.getPacket() instanceof ResourcePackSendS2CPacket) {
            currentAction = ResourcePackAction.ACCEPT;
            packetEvent.cancel();
        }
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        if (networkHandler != null) {
            if (currentAction == ResourcePackAction.ACCEPT) {
                networkHandler.sendPacket(new ResourcePackStatusC2SPacket(UUID.randomUUID(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
                currentAction = ResourcePackAction.SEND;
                counter.resetCounter();
            } else if (currentAction == ResourcePackAction.SEND && counter.isReached(300L)) {
                networkHandler.sendPacket(new ResourcePackStatusC2SPacket(UUID.randomUUID(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
                currentAction = ResourcePackAction.WAIT;
            }
        }
    }

    @Override
    public void deactivate() {
        currentAction = ResourcePackAction.WAIT;
        super.deactivate();
    }

    public enum ResourcePackAction {
        ACCEPT, SEND, WAIT;
    }
}
