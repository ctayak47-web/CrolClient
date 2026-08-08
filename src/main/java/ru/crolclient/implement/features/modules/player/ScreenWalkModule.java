package ru.crolclient.implement.features.modules.player;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.common.util.player.MovingUtil;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.implement.screens.menu.MenuScreen;

public class ScreenWalkModule extends Module {
    public ScreenWalkModule() {
        super("ScreenWalk", "Screen Walk", ModuleCategory.PLAYER);
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        GameOptions gameOptions = mc.options;


        if (shouldSkipExecution()) {
            return;
        }

        for (KeyBinding keyBinding : MovingUtil.getMovementKeys(false)) {
            long handle = mc.getWindow().getHandle();
            int keyCode = keyBinding.getDefaultKey().getCode();
            keyBinding.setPressed(InputUtil.isKeyPressed(handle, keyCode));
        }
    }

    public boolean shouldSkipExecution() {
        return mc.currentScreen == null
                || mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof SignEditScreen
                || mc.currentScreen instanceof AnvilScreen
                || mc.currentScreen instanceof AbstractCommandBlockScreen
                || mc.currentScreen instanceof StructureBlockScreen
                || mc.currentScreen instanceof MenuScreen;
    }
}
