
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.Hand;
import net.minecraft.StatusEffectInstance;
import net.minecraft.StatusEffects;
import net.minecraft.Slot;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PotionContentsComponent;
import net.minecraft.DataComponentTypes;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.utility.game.player.PlayerIntersectionUtil;
import crol.client.utility.game.player.PlayerInventoryUtil;

@ModuleAnnotation(name="Auto Inviz", category=Category.MOVEMENT, description="Пьёт зелье невидимости из хотбара.")
public final class AutoInviz
extends Module {
    public static final AutoInviz INSTANCE = new AutoInviz();
    private final ModeSetting useMode = new ModeSetting("Режим", "Слот", "Оффхенд");
    private boolean drinking;
    private boolean autoUsing;
    private int previousSelectedSlot = -1;
    private int forcedPotionSlot = -1;

    private AutoInviz() {
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (AutoInviz.mc.player == null || AutoInviz.mc.world == null || AutoInviz.mc.interactionManager == null) {
            this.stop(false);
            return;
        }
        if (AutoInviz.mc.currentScreen != null) {
            this.stop(true);
            return;
        }
        if (AutoInviz.mc.player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            this.stop(true);
            return;
        }
        if (AutoInviz.mc.player.isUsingItem()) {
            if (this.isInvisibilityPotion(AutoInviz.mc.player.getActiveItem())) {
                this.drinking = true;
                this.holdUseKey();
            } else {
                this.stop(true);
            }
            return;
        }
        if (this.useMode.is("Оффхенд")) {
            this.handleOffhandMode();
        } else {
            this.handleMainHandMode();
        }
    }

    private void handleMainHandMode() {
        if (!this.isInvisibilityPotion(AutoInviz.mc.player.getMainHandStack())) {
            int potionSlot = this.findHotbarInvisibilityPotionSlot();
            if (potionSlot == -1) {
                this.stop(true);
                return;
            }
            this.switchToSlot(potionSlot);
        }
        this.drinking = true;
        if (!AutoInviz.mc.player.isUsingItem()) {
            PlayerIntersectionUtil.useItem(Hand.MAIN_HAND);
        }
        this.holdUseKey();
    }

    private void handleOffhandMode() {
        if (!this.isInvisibilityPotion(AutoInviz.mc.player.getOffHandStack())) {
            int potionSlot = this.findHotbarInvisibilityPotionSlot();
            if (potionSlot == -1) {
                this.stop(true);
                return;
            }
            this.swapPotionToOffhand(potionSlot);
        }
        if (this.isInvisibilityPotion(AutoInviz.mc.player.getOffHandStack())) {
            this.drinking = true;
            if (!AutoInviz.mc.player.isUsingItem()) {
                PlayerIntersectionUtil.useItem(Hand.OFF_HAND);
            }
            this.holdUseKey();
            return;
        }
        this.handleMainHandMode();
    }

    private int findHotbarInvisibilityPotionSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = AutoInviz.mc.player.getInventory().getStack(slot);
            if (!this.isInvisibilityPotion(stack)) continue;
            return slot;
        }
        return -1;
    }

    private void switchToSlot(int slot) {
        if (AutoInviz.mc.player.getInventory().selectedSlot == slot) {
            this.forcedPotionSlot = slot;
            return;
        }
        if (this.previousSelectedSlot == -1) {
            this.previousSelectedSlot = AutoInviz.mc.player.getInventory().selectedSlot;
        }
        AutoInviz.mc.player.getInventory().selectedSlot = slot;
        this.forcedPotionSlot = slot;
    }

    private void swapPotionToOffhand(int hotbarSlot) {
        Slot slot = PlayerInventoryUtil.getSlot(s -> s.inventory == AutoInviz.mc.player.getInventory() && s.getIndex() == hotbarSlot);
        if (slot != null) {
            PlayerInventoryUtil.swapHand(slot, Hand.OFF_HAND, false);
        }
    }

    private boolean isInvisibilityPotion(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() != Items.POTION) {
            return false;
        }
        PotionContentsComponent component = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) {
            return false;
        }
        for (StatusEffectInstance effect : component.getEffects()) {
            if (effect.getEffectType() != StatusEffects.INVISIBILITY) continue;
            return true;
        }
        return false;
    }

    private void stop(boolean restoreSlot) {
        this.releaseUseKey();
        this.drinking = false;
        if (!restoreSlot || AutoInviz.mc.player == null) {
            if (!restoreSlot) {
                this.previousSelectedSlot = -1;
                this.forcedPotionSlot = -1;
            }
            return;
        }
        if (this.previousSelectedSlot != -1 && this.forcedPotionSlot != -1 && AutoInviz.mc.player.getInventory().selectedSlot == this.forcedPotionSlot) {
            AutoInviz.mc.player.getInventory().selectedSlot = this.previousSelectedSlot;
        }
        this.previousSelectedSlot = -1;
        this.forcedPotionSlot = -1;
    }

    private void holdUseKey() {
        AutoInviz.mc.options.useKey.setPressed(true);
        this.autoUsing = true;
    }

    private void releaseUseKey() {
        if (this.autoUsing) {
            AutoInviz.mc.options.useKey.setPressed(false);
            this.autoUsing = false;
        }
    }

    @Override
    public void onDisable() {
        this.stop(true);
        super.onDisable();
    }
}

