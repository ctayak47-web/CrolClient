package com.autobuy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafxmod.FXModLauncher;
import net.minecraftforge.fml.mod.event.FMLConstructModEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("autobuy")
public class AutoBuyMod {
    public static final String MOD_ID = "autobuy";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public AutoBuyMod() {
        IEventBus modEventBus = FXModLauncher.getInstance().getModEventBus(MOD_ID);
        
        ModLoadingContext.getInstance().registerConfig(
            ModConfig.Type.CLIENT, 
            AutoBuyConfig.CLIENT_SPEC
        );

        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("AutoBuy Mod initialized!");
        AutoBuyManager.init();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {
        
        @SubscribeEvent
        public static void onScreenOpen(ScreenEvent.Init.Post event) {
            AutoBuyManager.checkAuctionScreen(event.getScreen());
        }

        @SubscribeEvent
        public static void onTick(ScreenEvent.RenderEvent event) {
            AutoBuyManager.updateSearchAutomatic();
        }
    }
}
