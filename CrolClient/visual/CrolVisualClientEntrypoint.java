package crol.client;

import net.fabricmc.api.ClientModInitializer;
import crol.client.utility.input.KeybindManager;

public final class CrolClientEntrypoint
implements ClientModInitializer {
    public void onInitializeClient() {
        KeybindManager.init();
    }
}
