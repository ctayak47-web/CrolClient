package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.ClickEvent;
import crol.client.modules.Module;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("RETURN"))
    private void onKeyHook(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // Отправляем ивент всем модулям (например, для кейбиндера или записи макросов)
        CrolClient.INSTANCE.getEventManager().hookEvent(new ClickEvent(key, action));

        // GLFW.GLFW_PRESS вместо магической "1"
        if (action == GLFW.GLFW_PRESS) {

            // Если мы находимся в самой игре (не в меню, чате или инвентаре)
            if (MinecraftClient.getInstance().currentScreen == null) {

                // Проверяем, что клавиша валидная (не багованная)
                if (key > 0 && key < 1300) {
                    for (Module module : CrolClient.INSTANCE.getModuleManager().getModules()) {
                        // Если бинд модуля совпадает с нажатой клавишей - переключаем его состояние
                        if (module.getBind() == key) {
                            module.setEnabled(!module.isEnabled());
                        }
                    }
                }

            }
        }
    }
}