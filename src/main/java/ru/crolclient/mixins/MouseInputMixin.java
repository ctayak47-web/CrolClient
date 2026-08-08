package ru.crolclient.mixins;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.implement.events.keyboard.KeyEvent;
import ru.crolclient.api.event.EventManager;

@Mixin(Mouse.class)
public class MouseInputMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        EventManager.callEvent(new KeyEvent(button, action, true));

        for (Module module : Extra.getInstance().getModuleRepository().modules()) {
            if (module.getKey() == button) {
                if (module.getType() == 0) { // Hold mode
                    module.setState(action == 1);
                } else if (action == 1) { // Toggle mode
                    module.switchState();
                }
            }
        }
    }
}