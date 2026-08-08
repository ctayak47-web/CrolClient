package ru.crolclient.implement.features.modules.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.implement.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("all")
public class AutoEatModule extends Module {
    final ValueSetting hungerThreshold = new ValueSetting("Hunger threshold", "Hunger level to eat")
            .setValue(10)
            .range(1, 20);

    boolean eating = false;

    public AutoEatModule() {
        super("AutoEat", "Auto Eat", ModuleCategory.PLAYER);
        setup(hungerThreshold);
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.getValue()) {
            if (isHoldingEdibleItem()) {
                startEating();
            } else {
                stopEating();
            }
        } else {
            stopEating();
        }
        checkAndContinueEating();
    }

    public void checkAndContinueEating() {
        if (eating && mc.currentScreen == null && !mc.player.isUsingItem()) {
            mc.options.useKey.setPressed(true);
        }
    }

    public void startEating() {
        if (!eating && mc.currentScreen == null && !mc.player.isUsingItem()) {
            eating = true;
            mc.options.useKey.setPressed(true);
        }
    }

    public void stopEating() {
        if (eating) {
            eating = false;
            mc.options.useKey.setPressed(false);
        }
    }

    private boolean isHoldingEdibleItem() {
        return isEdibleItem(mc.player.getMainHandStack()) || isEdibleItem(mc.player.getOffHandStack());
    }

    private boolean isEdibleItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return stack.get(DataComponentTypes.FOOD) != null && !isHarmfulFood(item);
    }

    private boolean isHarmfulFood(Item item) {
        return item == Items.PORKCHOP || item == Items.BEEF || item == Items.MUTTON ||
                item == Items.CHICKEN || item == Items.SALMON || item == Items.COD;
    }
}
