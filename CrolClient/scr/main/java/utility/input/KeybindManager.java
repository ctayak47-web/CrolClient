
package crol.client.utility.input;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.KeyBinding;
import net.minecraft.MinecraftClient;
import net.minecraft.InputUtil;
import net.minecraft.Screen;
import crol.client.CrolClient;
import crol.client.screens.menu.MenuScreen;

public final class KeybindManager {
    private static final String CATEGORY_KEY = "category.CrolClient.controls";
    private static final String OPEN_CLICK_GUI_KEY = "key.CrolClient.open_click_gui";
    private static final int FALLBACK_KEY = 344;
    private static boolean initialized;
    private static KeyBinding openClickGuiBind;

    private KeybindManager() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            openClickGuiBind = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding(OPEN_CLICK_GUI_KEY, InputUtil.Type.KEYSYM, 344, CATEGORY_KEY));
        }
        catch (RuntimeException ignored) {
            openClickGuiBind = new KeyBinding(OPEN_CLICK_GUI_KEY, InputUtil.Type.KEYSYM, 344, CATEGORY_KEY);
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openClickGuiBind.wasPressed()) {
                KeybindManager.openClickGui(client);
            }
        });
    }

    public static void onRawKeyInput(int keyCode, int action) {
        if (action != 1 || !KeybindManager.isBound(InputUtil.Type.KEYSYM, keyCode)) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) {
            return;
        }
        KeybindManager.openClickGui(client);
    }

    public static void onRawMouseInput(int button, int action) {
        if (action != 1 || !KeybindManager.isBound(InputUtil.Type.MOUSE, button)) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) {
            return;
        }
        KeybindManager.openClickGui(client);
    }

    private static boolean isBound(InputUtil.Type type, int code) {
        if (openClickGuiBind == null) {
            return false;
        }
        if (type == InputUtil.Type.KEYSYM) {
            return openClickGuiBind.matchesKey(code, -1);
        }
        if (type == InputUtil.Type.MOUSE) {
            return openClickGuiBind.matchesMouse(code);
        }
        return false;
    }

    private static void openClickGui(MinecraftClient client) {
        if (client == null || client.world == null || client.player == null) {
            return;
        }
        MenuScreen menuScreen = CrolClient.getInstance().getMenuScreen();
        if (menuScreen == null || client.currentScreen == menuScreen) {
            return;
        }
        menuScreen.setClosing(false);
        client.setScreen((Screen)menuScreen);
    }
}

