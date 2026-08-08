package ru.crolclient.common.util.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.packet.Packet;
import ru.crolclient.mixins.accessors.IClientWorldMixin;

public interface IHolder {

    MinecraftClient mc = MinecraftClient.getInstance();

    static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    static void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendPacket(packet);
    }

    static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (mc.getNetworkHandler() == null || mc.world == null) return;
        try (PendingUpdateManager pendingUpdateManager = ((IClientWorldMixin) mc.world).getPendingUpdateManager().incrementSequence();){
            int i = pendingUpdateManager.getSequence();
            mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
        }
    }
}