
package CrolClient.visual;

import net.fabricmc.api.ClientModInitializer;
import CrolClient.visual.utility.input.KeybindManager;

public final class CrolClientVisualClientEntrypoint
implements ClientModInitializer {
    public void onInitializeClient() {
        KeybindManager.init();
    }
}
