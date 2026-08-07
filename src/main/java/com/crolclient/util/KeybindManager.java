package com.crolclient.util;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
public class KeybindManager {
    private static KeyBinding menuKey;
    public static void register() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.crolclient.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.crolclient.general"
        ));
    }
    public static boolean isMenuKeyPressed() {
        return menuKey != null && menuKey.wasPressed();
    }
}
