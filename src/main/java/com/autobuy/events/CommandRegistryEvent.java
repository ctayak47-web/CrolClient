package com.autobuy.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import com.autobuy.AutoBuyMod;
import com.autobuy.commands.AutoBuyCommand;

@Mod.EventBusSubscriber(modid = AutoBuyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CommandRegistryEvent {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterClientCommandsEvent event) {
        AutoBuyCommand.register(event.getDispatcher());
        AutoBuyMod.LOGGER.info("AutoBuy commands registered!");
    }
}
