package com.autobuy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import java.util.*;
import com.autobuy.data.AutoBuyItem;
import com.autobuy.storage.AutoBuyStorage;

public class AutoBuyManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean enabled = false;
    private static long lastSearchTime = 0;
    private static long lastAntiLagTime = 0;
    private static int currentAccountIndex = 0;
    private static AutoBuyStorage storage = new AutoBuyStorage();
    
    public static void init() {
        storage.load();
    }

    public static void startAutoBuy() {
        enabled = true;
        lastSearchTime = System.currentTimeMillis();
        AutoBuyMod.LOGGER.info("AutoBuy started!");
    }

    public static void stopAutoBuy() {
        enabled = false;
        AutoBuyMod.LOGGER.info("AutoBuy stopped!");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void addItem(String itemName, long minPrice, long maxPrice) {
        AutoBuyItem item = new AutoBuyItem(itemName, minPrice, maxPrice);
        storage.addItem(item);
        storage.save();
        showMessage("§aДобавлен: " + itemName + " (" + minPrice + "-" + maxPrice + ")");
    }

    public static void removeItem(String itemName) {
        storage.removeItem(itemName);
        storage.save();
        showMessage("§cУдален: " + itemName);
    }

    public static void deleteAllItems() {
        storage.clear();
        storage.save();
        showMessage("§cВсе предметы удалены");
    }

    public static List<AutoBuyItem> getItems() {
        return storage.getItems();
    }

    public static void updateSearchAutomatic() {
        if (!enabled) return;

        long currentTime = System.currentTimeMillis();
        long interval = AutoBuyConfig.SEARCH_INTERVAL.get() * 50L; // Convert ticks to ms

        // Проверка времени последнего поиска
        if (currentTime - lastSearchTime >= interval) {
            performSearch();
            lastSearchTime = currentTime;
        }

        // Антилаг ротация (только для Fantay)
        if (storage.getServerType() == AutoBuyStorage.ServerType.FANTAY) {
            if (currentTime - lastAntiLagTime >= AutoBuyConfig.ANTI_LAG_INTERVAL.get() * 50L) {
                performAntiLagRotation();
                lastAntiLagTime = currentTime;
            }
        }
    }

    private static void performSearch() {
        if (storage.getItems().isEmpty()) return;

        try {
            // Поиск каждого предмета
            for (AutoBuyItem item : storage.getItems()) {
                String command = String.format("/ah search %s", item.getName());
                sendCommand(command);
                
                // Небольшая задержка между командами для обработки
                Thread.sleep(500);
            }
        } catch (Exception e) {
            AutoBuyMod.LOGGER.error("Error during search", e);
        }
    }

    private static void performAntiLagRotation() {
        // Ротация между аккаунтами на Fantay
        // Команды /an103 до /an505
        int accountNumber = 103 + (currentAccountIndex % 403);
        String command = "/an" + accountNumber;
        
        sendCommand(command);
        currentAccountIndex++;
        
        AutoBuyMod.LOGGER.info("Anti-lag rotation: " + command);
    }

    public static void checkAuctionScreen(Object screen) {
        if (screen == null) return;
        
        String screenName = screen.getClass().getSimpleName();
        
        // Детект ауксчика Fantay
        if (screenName.contains("Auction") || screenName.contains("Chest")) {
            storage.detectServer(AutoBuyStorage.ServerType.FANTAY);
        }
    }

    private static void sendCommand(String command) {
        if (mc.player != null) {
            mc.player.connection.sendCommand(command.substring(1)); // Remove leading /
            showMessage("§7> " + command);
        }
    }

    private static void showMessage(String message) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§c[AutoBuy]§r " + message), false);
        }
    }

    public static AutoBuyStorage getStorage() {
        return storage;
    }
}
