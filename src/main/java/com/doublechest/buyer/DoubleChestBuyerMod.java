package com.doublechest.buyer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;

import com.doublechest.buyer.gui.BuyerScreen;
import com.doublechest.buyer.data.BuyerData;

public class DoubleChestBuyerMod implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		BuyerData.load();
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
				LiteralArgumentBuilder.<CommandSource>literal("buyer")
					.executes(context -> {
						var client = net.minecraft.client.MinecraftClient.getInstance();
						if (client.player != null) {
							client.setScreen(new BuyerScreen());
						}
						return 1;
					})
			);
		});
	}
}
