package com.crolclient;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.FeatureManager;
import com.crolclient.gui.CrolMenuScreen;
import com.crolclient.hud.HUDManager;
import com.crolclient.util.KeybindManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class CrolClientClient implements ClientModInitializer {
    public static final String MOD_ID = "crolclient";
    public static CrolClientClient INSTANCE;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        ConfigManager.load();
        FeatureManager.init();
        HUDManager.init();
        KeybindManager.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (KeybindManager.isMenuKeyPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new CrolMenuScreen());
                }
            }
            FeatureManager.onTick(client);
        });
    }
}
