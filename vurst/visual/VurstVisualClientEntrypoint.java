
package vurst.visual;

import net.fabricmc.api.ClientModInitializer;
import vurst.visual.utility.input.KeybindManager;

public final class VurstVisualClientEntrypoint
implements ClientModInitializer {
    public void onInitializeClient() {
        KeybindManager.init();
    }
}

