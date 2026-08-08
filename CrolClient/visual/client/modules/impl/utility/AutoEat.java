
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.Hand;
import net.minecraft.Slot;
import net.minecraft.ItemStack;
import net.minecraft.FoodComponent;
import net.minecraft.DataComponentTypes;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.game.player.PlayerInventoryUtil;

@ModuleAnnotation(name="Auto Eat", category=Category.MOVEMENT, description="Авто-еда из хотбара.")
public final class AutoEat
extends Module {
    public static final AutoEat INSTANCE = new AutoEat();
    private final NumberSetting hungerLevel = new NumberSetting("Голод", 15.0f, 1.0f, 20.0f, 1.0f);
    private final ModeSetting eatMode = new ModeSetting("Режим", "Слот", "Оффхенд");
    private boolean autoEating;
    private int previousSelectedSlot = -1;
    private int forcedEatSlot = -1;

    private AutoEat() {
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        boolean shouldEat;
        if (AutoEat.mc.player == null || AutoEat.mc.world == null) {
            this.stopEating(false);
            return;
        }
        if (AutoEat.mc.currentScreen != null) {
            this.stopEating(true);
            return;
        }
        boolean bl = shouldEat = (float)AutoEat.mc.player.getHungerManager().getFoodLevel() <= this.hungerLevel.getCurrent() && AutoEat.mc.player.canConsume(false);
        if (!shouldEat) {
            this.stopEating(true);
            return;
        }
        if (!this.prepareFoodFromHotbar()) {
            this.stopEating(true);
            return;
        }
        AutoEat.mc.options.useKey.setPressed(true);
        this.autoEating = true;
    }

    private boolean prepareFoodFromHotbar() {
        if (this.isFood(AutoEat.mc.player.getOffHandStack()) || this.isFood(AutoEat.mc.player.getMainHandStack())) {
            return true;
        }
        int hotbarFoodSlot = this.findBestFoodHotbarSlot();
        if (hotbarFoodSlot == -1) {
            return false;
        }
        if (this.eatMode.is("Оффхенд")) {
            this.swapHotbarFoodToOffhand(hotbarFoodSlot);
            if (!this.isFood(AutoEat.mc.player.getOffHandStack()) && !this.isFood(AutoEat.mc.player.getMainHandStack())) {
                this.switchToHotbarSlot(hotbarFoodSlot);
            }
        } else {
            this.switchToHotbarSlot(hotbarFoodSlot);
        }
        return this.isFood(AutoEat.mc.player.getOffHandStack()) || this.isFood(AutoEat.mc.player.getMainHandStack());
    }

    private int findBestFoodHotbarSlot() {
        int bestSlot = -1;
        float bestSaturation = -1.0f;
        for (int slot = 0; slot < 9; ++slot) {
            float saturation;
            ItemStack stack = AutoEat.mc.player.getInventory().getStack(slot);
            FoodComponent food = (FoodComponent)stack.get(DataComponentTypes.FOOD);
            if (food == null || stack.isEmpty() || !((saturation = food.comp_2492()) > bestSaturation)) continue;
            bestSaturation = saturation;
            bestSlot = slot;
        }
        return bestSlot;
    }

    private void switchToHotbarSlot(int slot) {
        if (AutoEat.mc.player.getInventory().selectedSlot == slot) {
            this.forcedEatSlot = slot;
            return;
        }
        if (this.previousSelectedSlot == -1) {
            this.previousSelectedSlot = AutoEat.mc.player.getInventory().selectedSlot;
        }
        AutoEat.mc.player.getInventory().selectedSlot = slot;
        this.forcedEatSlot = slot;
    }

    private void swapHotbarFoodToOffhand(int hotbarSlot) {
        Slot slot = PlayerInventoryUtil.getSlot(s -> s.inventory == AutoEat.mc.player.getInventory() && s.getIndex() == hotbarSlot);
        if (slot != null) {
            PlayerInventoryUtil.swapHand(slot, Hand.OFF_HAND, false);
        }
    }

    private boolean isFood(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.get(DataComponentTypes.FOOD) != null;
    }

    private void stopEating(boolean restoreSlot) {
        if (this.autoEating) {
            AutoEat.mc.options.useKey.setPressed(false);
            this.autoEating = false;
        }
        if (!restoreSlot || AutoEat.mc.player == null) {
            if (!restoreSlot) {
                this.previousSelectedSlot = -1;
                this.forcedEatSlot = -1;
            }
            return;
        }
        if (this.previousSelectedSlot != -1 && this.forcedEatSlot != -1 && AutoEat.mc.player.getInventory().selectedSlot == this.forcedEatSlot) {
            AutoEat.mc.player.getInventory().selectedSlot = this.previousSelectedSlot;
        }
        this.previousSelectedSlot = -1;
        this.forcedEatSlot = -1;
    }

    @Override
    public void onDisable() {
        this.stopEating(true);
        super.onDisable();
    }
}

