package ru.crolclient.implement.features.modules.combat;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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
public class AutoGappleModule extends Module {
    final ValueSetting healthThreshold = new ValueSetting("Health threshold", "Health level to eat")
            .setValue(10)
            .range(1, 20)
            .increment(0.5f);

    boolean eating = false;

    public AutoGappleModule() {
        super("AutoGapple", "Auto Gapple", ModuleCategory.COMBAT);
        setup(healthThreshold);
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (mc.player.getHealth() < healthThreshold.getValue()) {
            if (isHoldingGoldenApple() && !isGoldenAppleOnCooldown()) {
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

    private boolean isHoldingGoldenApple() {
        return isGoldenApple(mc.player.getMainHandStack()) || isGoldenApple(mc.player.getOffHandStack());
    }

    private boolean isGoldenApple(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    private boolean isGoldenAppleOnCooldown() {
        return mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.GOLDEN_APPLE)) ||
                mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
    }
}
