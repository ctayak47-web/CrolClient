
package crol.client.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.base.events.impl.input.EventKey;
import crol.client.modules.impl.utility.FastSwap;
import crol.client.utility.input.KeybindManager;

@Mixin(value={Keyboard.class})
public class KeyboardMixin {
    @Inject(method={"onKey"}, at={@At(value="HEAD")})
    public void triggerKeyEvent(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key == -1) {
            return;
        }
        EventManager.call(new EventKey(action, key));
        KeybindManager.onRawKeyInput(key, action);
        FastSwap.onRawKeyInput(key, action);
    }
}

