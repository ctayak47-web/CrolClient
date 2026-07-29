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

            // Проверяем уровень голода игрока (если меньше или равен 14 из 20, то можно есть)
            if (client.player.getHungerManager().isFoodLevelInsufficient()) {
                // Ищем еду в инвентаре или руках
                for (int i = 0; i < 9; i++) { // Проверяем хотбар
                    ItemStack stack = client.player.getInventory().getStack(i);
                    
                    // Проверка через DataComponentTypes.FOOD для Minecraft 1.21
                    if (stack.contains(DataComponentTypes.FOOD)) {
                        int oldSlot = client.player.getInventory().selectedSlot;
                        client.player.getInventory().selectedSlot = i;
                        
                        // Симулируем удержание кнопки использования предмета (правый клик)
                        client.options.useKey.setPressed(true);
                        
                        // Возвращаем слот обратно (или оставляем, в зависимости от твоей логики)
                        // client.interactionManager.interactItem(client.player, net.minecraft.util.Hand.MAIN_HAND);
                        
                        break;
                    }
                }
            } else {
                // Отпускаем кнопку, когда сыт
                if (client.options.useKey.isPressed()) {
                    client.options.useKey.setPressed(false);
                }
            }
        });
    }
}
