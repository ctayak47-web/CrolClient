package ru.crolclient.core.listener.impl;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.core.Extra;
import ru.crolclient.core.listener.Listener;
import ru.crolclient.implement.events.packet.PacketEvent;


public class PacketEventListener implements Listener {
    public static boolean serverSprint;

    @EventHandler
    public void onPacket(PacketEvent packetEvent) {
        Packet<?> packet = packetEvent.getPacket();
        if (packet instanceof ClientCommandC2SPacket clientCommandC2SPacket) {
            if (clientCommandC2SPacket.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                serverSprint = true;
            }
            if (clientCommandC2SPacket.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
                serverSprint = false;
            }
        }
        Extra.getInstance().getAttackPerpetrator().onPacket(packetEvent);
    }
}
