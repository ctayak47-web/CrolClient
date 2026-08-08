
package crol.client.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.MinecraftClient;
import net.minecraft.Mouse;
import net.minecraft.Screen;
import net.minecraft.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.base.events.impl.input.EventHotBarScroll;
import crol.client.base.events.impl.input.EventKey;
import crol.client.base.events.impl.input.EventMouse;
import crol.client.base.events.impl.input.EventMouseRotation;
import crol.client.modules.impl.utility.FastSwap;
import crol.client.utility.input.KeybindManager;
import crol.client.utility.interfaces.IMinecraft;

@Mixin(value={Mouse.class}, priority=500)
public class MouseMixin {
    @Shadow
    private int activeButton;

    @Inject(method={"onMouseButton"}, at={@At(value="HEAD")})
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (button != -1 && window == IMinecraft.mc.getWindow().getHandle()) {
            EventManager.call(new EventKey(action, button));
            EventManager.call(new EventMouse(button, action));
            KeybindManager.onRawMouseInput(button, action);
            FastSwap.onRawMouseInput(button, action);
        }
    }

    @Inject(method={"onMouseScroll"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;")}, cancellable=true)
    public void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
        EventHotBarScroll event = new EventHotBarScroll(horizontal, vertical);
        EventManager.call(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/Mouse;isCursorLocked()Z"))
    public boolean onIsCursorLocked(Mouse instance) {
        return instance.isCursorLocked() || this.isAnim();
    }

    @Redirect(method={"updateMouse"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), require=0)
    private void modifyMouseRotationInput(ClientPlayerEntity instance, double cursorDeltaX, double cursorDeltaY) {
        EventMouseRotation event = new EventMouseRotation((float)cursorDeltaX, (float)cursorDeltaY);
        EventManager.call(event);
        if (!event.isCancelled()) {
            instance.changeLookDirection((double)event.getCursorDeltaX(), (double)event.getCursorDeltaY());
        }
    }

    @Unique
    private boolean isAnim() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        return false;
    }
}

