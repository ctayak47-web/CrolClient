package com.crolclient.feature.autoeat;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class AutoEatFeature extends Feature {
    
    public AutoEatFeature() {
        super("Auto Eat", "Automatically eat when hungry", FeatureCategory.UTIL);
        registerListeners();
    }

    @Override
    protected void onEnable() {
        ConfigManager.getConfig().autoEatEnabled = true;
        ConfigManager.save();
    }

    @Override
    protected void onDisable() {
        ConfigManager.getConfig().autoEatEnabled = false;
        ConfigManager.save();
    }

    private void registerListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isEnabled() || client.player == null || client.interactionManager == null) return;

            // Проверяем уровень сытости (если меньше 20, персонаж может есть)
            if (client.player.getHungerManager().getFoodLevel() < 20) {
                // Ищем съедобный предмет в хотбаре (слоты 0-8)
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = client.player.getInventory().getStack(i);
                    
                    // Безопасная проверка еды через компонент для версии 1.21
                    if (stack.contains(DataComponentTypes.FOOD)) {
                        client.player.getInventory().selectedSlot = i;
                        // Зажимаем правую кнопку мыши (использование предмета)
                        client.options.useKey.setPressed(true);
                        break;
                    }
                }
            } else {
                // Когда сытость полная, отпускаем кнопку
                if (client.options.useKey.isPressed()) {
                    client.options.useKey.setPressed(false);
                }
            }
        });
    }
}
