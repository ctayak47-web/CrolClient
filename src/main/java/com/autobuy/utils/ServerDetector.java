package com.autobuy.utils;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.Minecraft;
import com.autobuy.storage.AutoBuyStorage;

public class ServerDetector {
    private static final Minecraft mc = Minecraft.getInstance();

    public static AutoBuyStorage.ServerType detectServer() {
        if (mc.getCurrentServer() == null) {
            return AutoBuyStorage.ServerType.UNKNOWN;
        }

        String serverAddress = mc.getCurrentServer().ip.toLowerCase();
        String serverName = mc.getCurrentServer().name.toLowerCase();

        // Fantay detection
        if (serverAddress.contains("fantay") || 
            serverName.contains("fantay") ||
            serverAddress.contains("fantasycraft")) {
            return AutoBuyStorage.ServerType.FANTAY;
        }

        // HolyWorld detection
        if (serverAddress.contains("holyworld") || 
            serverName.contains("holyworld") ||
            serverAddress.contains("holy")) {
            return AutoBuyStorage.ServerType.HOLYWORLD;
        }

        return AutoBuyStorage.ServerType.UNKNOWN;
    }

    public static boolean isFantay() {
        return detectServer() == AutoBuyStorage.ServerType.FANTAY;
    }

    public static boolean isHolyWorld() {
        return detectServer() == AutoBuyStorage.ServerType.HOLYWORLD;
    }
}
