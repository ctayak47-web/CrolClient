package com.autobuy.antilag;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.autobuy.AutoBuyMod;

import java.util.*;

/**
 * Система анти-лага для Fantay сервера
 * Ротирует между аккаунтами (/an103 - /an505) для избежания бана за TM+
 */
public class FantayAntiLag {
    private static final Minecraft mc = Minecraft.getInstance();
    
    // Диапазон аккаунтов на Fantay
    private static final int MIN_ACCOUNT = 103;
    private static final int MAX_ACCOUNT = 505;
    private static final int MAX_PLAYERS_PER_ACCOUNT = 2; // Макс игроков на аккаунте до бана за TM+
    
    private static int currentAccountIndex = 0;
    private static long lastRotationTime = 0;
    private static long rotationInterval = 30000; // 30 сек по умолчанию
    private static Map<Integer, Integer> accountPlayerCount = new HashMap<>();
    
    public static void initialize() {
        // Инициализируем счетчик игроков для всех аккаунтов
        for (int i = MIN_ACCOUNT; i <= MAX_ACCOUNT; i++) {
            accountPlayerCount.put(i, 0);
        }
        AutoBuyMod.LOGGER.info("FantayAntiLag initialized");
    }
    
    /**
     * Главный метод обновления анти-лага
     * Вызывается каждый тик
     */
    public static void update() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastRotationTime >= rotationInterval) {
            performRotation();
            lastRotationTime = currentTime;
        }
    }
    
    /**
     * Выполнить ротацию на следующий аккаунт
     */
    private static void performRotation() {
        int nextAccount = getNextAvailableAccount();
        
        if (nextAccount != -1) {
            switchAccount(nextAccount);
        }
    }
    
    /**
     * Получить следующий доступный аккаунт
     * @return номер аккаунта или -1 если все заняты
     */
    private static int getNextAvailableAccount() {
        // Сначала ищем аккаунты с 0-1 игроком
        for (int i = MIN_ACCOUNT; i <= MAX_ACCOUNT; i++) {
            if (accountPlayerCount.getOrDefault(i, 0) < MAX_PLAYERS_PER_ACCOUNT) {
                return i;
            }
        }
        
        // Если все заняты, берем первый с минимальным количеством
        int minAccount = MIN_ACCOUNT;
        int minCount = Integer.MAX_VALUE;
        
        for (Map.Entry<Integer, Integer> entry : accountPlayerCount.entrySet()) {
            if (entry.getValue() < minCount) {
                minCount = entry.getValue();
                minAccount = entry.getKey();
            }
        }
        
        return minAccount;
    }
    
    /**
     * Переключиться на другой аккаунт
     */
    private static void switchAccount(int accountNumber) {
        if (mc.player == null) return;
        
        String command = "/an" + accountNumber;
        mc.player.connection.sendCommand(command.substring(1));
        
        // Логируем для отладки
        AutoBuyMod.LOGGER.info("Switched to account: " + accountNumber);
        showMessage("§a✓ Переключились на аккаунт: /an" + accountNumber);
    }
    
    /**
     * Обновить количество игроков на аккаунте
     */
    public static void updateAccountPlayerCount(int accountNumber, int playerCount) {
        accountPlayerCount.put(accountNumber, playerCount);
        
        if (playerCount >= MAX_PLAYERS_PER_ACCOUNT) {
            AutoBuyMod.LOGGER.warn("Account " + accountNumber + " is full! (" + playerCount + " players)");
            showMessage("§c⚠ Аккаунт /an" + accountNumber + " переполнен!");
        }
    }
    
    /**
     * Установить интервал ротации
     */
    public static void setRotationInterval(long intervalMs) {
        rotationInterval = intervalMs;
        AutoBuyMod.LOGGER.info("Rotation interval set to: " + intervalMs + "ms");
    }
    
    /**
     * Получить текущий интервал ротации
     */
    public static long getRotationInterval() {
        return rotationInterval;
    }
    
    /**
     * Получить текущий аккаунт
     */
    public static int getCurrentAccount() {
        return MIN_ACCOUNT + currentAccountIndex;
    }
    
    /**
     * Получить количество игроков на аккаунте
     */
    public static int getAccountPlayerCount(int accountNumber) {
        return accountPlayerCount.getOrDefault(accountNumber, 0);
    }
    
    /**
     * Получить статистику всех аккаунтов
     */
    public static String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("§c=== FantayAntiLag Statistics ===\n");
        sb.append("§7Rotation Interval: §f").append(rotationInterval).append("ms\n");
        sb.append("§7Account Range: §f/an").append(MIN_ACCOUNT).append(" - /an").append(MAX_ACCOUNT).append("\n");
        sb.append("§7Total Accounts: §f").append(MAX_ACCOUNT - MIN_ACCOUNT + 1).append("\n\n");
        
        // Группируем аккаунты по загруженности
        int empty = 0;
        int onePlayer = 0;
        int full = 0;
        
        for (Integer count : accountPlayerCount.values()) {
            if (count == 0) empty++;
            else if (count == 1) onePlayer++;
            else if (count >= MAX_PLAYERS_PER_ACCOUNT) full++;
        }
        
        sb.append("§7Статус:\n");
        sb.append("§a  Свободные: §f").append(empty).append("\n");
        sb.append("§e  1 игрок: §f").append(onePlayer).append("\n");
        sb.append("§c  Переполненные: §f").append(full).append("\n");
        
        return sb.toString();
    }
    
    private static void showMessage(String message) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§c[AntiLag]§r " + message), false);
        }
    }
}
