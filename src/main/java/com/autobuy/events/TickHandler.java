package com.autobuy.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.gui.screens.ChatScreen;
import com.autobuy.AutoBuyMod;
import com.autobuy.AutoBuyManager;
import com.autobuy.auction.AuctionIntegration;
import com.autobuy.antilag.FantayAntiLag;
import com.autobuy.scanner.AuctionScanner;

/**
 * Обработчик тиков для автоматизации процессов
 */
@Mod.EventBusSubscriber(modid = AutoBuyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TickHandler {
    private static int tickCounter = 0;
    private static final int CLEANUP_INTERVAL = 200; // Очистка кэша каждые 10 сек

    @SubscribeEvent
    public static void onScreenEvent(ScreenEvent.Init.Post event) {
        // Обновляем информацию об ауксчике
        AuctionIntegration.updateAuctionState(event.getScreen());
    }

    @SubscribeEvent
    public static void onRenderTick(ScreenEvent.RenderEvent event) {
        tickCounter++;

        // Основной цикл автобая
        AutoBuyManager.updateSearchAutomatic();

        // Обновление антилага (для Fantay)
        if (AutoBuyManager.getStorage().getServerType() == com.autobuy.storage.AutoBuyStorage.ServerType.FANTAY) {
            FantayAntiLag.update();
        }

        // Периодическая очистка кэша
        if (tickCounter % CLEANUP_INTERVAL == 0) {
            AuctionScanner.cleanExpiredCache();
            tickCounter = 0;
        }
    }
}
