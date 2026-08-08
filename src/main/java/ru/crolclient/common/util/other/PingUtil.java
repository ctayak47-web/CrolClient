package ru.crolclient.common.util.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

public class PingUtil {

    public static int getPing() {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.getNetworkHandler() != null && mc.player != null) {
            ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
            PlayerListEntry entry = networkHandler.getPlayerListEntry(mc.player.getUuid());

            if (entry != null) {
                return entry.getLatency();
            }
        }

        return -1;
    }
}
