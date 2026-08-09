package com.autobuy.auction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.autobuy.AutoBuyMod;

/**
 * Интеграция с GUI аукциона
 * Поддерживает Fantay и HolyWorld ауксчики
 */
public class AuctionIntegration {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean isAuctionScreenOpen = false;
    private static AuctionScreenType currentScreenType = AuctionScreenType.UNKNOWN;
    
    public enum AuctionScreenType {
        FANTAY_SEARCH,
        FANTAY_BROWSE,
        HOLYWORLD_SEARCH,
        HOLYWORLD_BROWSE,
        UNKNOWN
    }
    
    /**
     * Проверить, открыт ли ауксчик
     */
    public static boolean isAuctionOpen() {
        return isAuctionScreenOpen;
    }
    
    /**
     * Получить тип открытого ауксчика
     */
    public static AuctionScreenType getAuctionType() {
        return currentScreenType;
    }
    
    /**
     * Обновить состояние ауксчика
     */
    public static void updateAuctionState(Screen screen) {
        if (screen == null) {
            isAuctionScreenOpen = false;
            currentScreenType = AuctionScreenType.UNKNOWN;
            return;
        }
        
        String screenClass = screen.getClass().getSimpleName();
        String screenName = screen.getClass().getName();
        
        // Детект Fantay ауксчика
        if (isFantayAuction(screenClass, screenName)) {
            isAuctionScreenOpen = true;
            
            if (screenName.contains("Browse")) {
                currentScreenType = AuctionScreenType.FANTAY_BROWSE;
            } else if (screenName.contains("Search")) {
                currentScreenType = AuctionScreenType.FANTAY_SEARCH;
            } else {
                currentScreenType = AuctionScreenType.FANTAY_BROWSE;
            }
            
            return;
        }
        
        // Детект HolyWorld ауксчика
        if (isHolyWorldAuction(screenClass, screenName)) {
            isAuctionScreenOpen = true;
            
            if (screenName.contains("Browse")) {
                currentScreenType = AuctionScreenType.HOLYWORLD_BROWSE;
            } else if (screenName.contains("Search")) {
                currentScreenType = AuctionScreenType.HOLYWORLD_SEARCH;
            } else {
                currentScreenType = AuctionScreenType.HOLYWORLD_BROWSE;
            }
            
            return;
        }
        
        isAuctionScreenOpen = false;
        currentScreenType = AuctionScreenType.UNKNOWN;
    }
    
    /**
     * Проверить, является ли это Fantay ауксчиком
     */
    private static boolean isFantayAuction(String screenClass, String screenName) {
        return screenName.toLowerCase().contains("auction") ||
               screenName.toLowerCase().contains("shop") ||
               screenClass.contains("Chest") ||
               screenClass.contains("Container");
    }
    
    /**
     * Проверить, является ли это HolyWorld ауксчиком
     */
    private static boolean isHolyWorldAuction(String screenClass, String screenName) {
        return screenName.toLowerCase().contains("holyworld") ||
               screenName.toLowerCase().contains("merchant") ||
               screenClass.contains("Auction");
    }
    
    /**
     * Отправить команду поиска товара
     */
    public static void searchItem(String itemName) {
        if (mc.player == null) return;
        
        String command = "/ah search " + itemName;
        mc.player.connection.sendCommand(command.substring(1));
        
        AutoBuyMod.LOGGER.info("Searching for: " + itemName);
    }
    
    /**
     * Купить предмет (имитация)
     */
    public static void buyItem(String seller, int price) {
        if (mc.player == null) return;
        
        // TODO: Реализовать клик по нужному лоту
        String message = String.format("§7[Auction]§r Попытка покупки от %s за %d", seller, price);
        mc.player.displayClientMessage(Component.literal(message), false);
    }
    
    /**
     * Получить полную статистику ауксчика
     */
    public static String getAuctionStatus() {
        return String.format(
            "§7[Auction] Открыт: %s | Тип: %s",
            isAuctionScreenOpen ? "§aДа" : "§cНет",
            currentScreenType.name()
        );
    }
}
