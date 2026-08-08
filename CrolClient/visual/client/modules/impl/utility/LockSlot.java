
package crol.client.modules.impl.utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.PlayerInventory;
import net.minecraft.Slot;
import net.minecraft.BlockItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Blocks;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;

@ModuleAnnotation(name="Lock Slot", category=Category.MOVEMENT, description="Блокирует выброс предметов из выбранных слотов хотбара.")
public final class LockSlot
extends Module {
    public static final LockSlot INSTANCE = new LockSlot();
    private final BooleanSetting slot1 = new BooleanSetting("Слот 1", false);
    private final BooleanSetting slot2 = new BooleanSetting("Слот 2", false);
    private final BooleanSetting slot3 = new BooleanSetting("Слот 3", false);
    private final BooleanSetting slot4 = new BooleanSetting("Слот 4", false);
    private final BooleanSetting slot5 = new BooleanSetting("Слот 5", false);
    private final BooleanSetting slot6 = new BooleanSetting("Слот 6", false);
    private final BooleanSetting slot7 = new BooleanSetting("Слот 7", false);
    private final BooleanSetting slot8 = new BooleanSetting("Слот 8", false);
    private final BooleanSetting slot9 = new BooleanSetting("Слот 9", false);
    private final BooleanSetting protectTalismans = new BooleanSetting("Не выбрасывать тотемы/сферы", true);

    private LockSlot() {
    }

    public boolean shouldBlockDrop(Slot slot) {
        if (!this.isEnabled() || slot == null || !slot.hasStack()) {
            return false;
        }
        ItemStack stack = slot.getStack();
        if (this.protectTalismans.isEnabled() && this.isProtectedStack(stack)) {
            return true;
        }
        return this.isLockedHotbarSlot(slot);
    }

    public boolean shouldBlockHotbarDrop(int hotbarIndex, ItemStack stack) {
        if (!this.isEnabled() || stack == null || stack.isEmpty()) {
            return false;
        }
        if (this.protectTalismans.isEnabled() && this.isProtectedStack(stack)) {
            return true;
        }
        return this.isLockedHotbarIndex(hotbarIndex);
    }

    private boolean isLockedHotbarSlot(Slot slot) {
        if (!(slot.inventory instanceof PlayerInventory)) {
            return false;
        }
        int index = this.getSlotIndex(slot);
        if (index < 0 || index > 8) {
            return false;
        }
        return this.isLockedHotbarIndex(index);
    }

    private boolean isLockedHotbarIndex(int hotbarIndex) {
        return switch (hotbarIndex) {
            case 0 -> this.slot1.isEnabled();
            case 1 -> this.slot2.isEnabled();
            case 2 -> this.slot3.isEnabled();
            case 3 -> this.slot4.isEnabled();
            case 4 -> this.slot5.isEnabled();
            case 5 -> this.slot6.isEnabled();
            case 6 -> this.slot7.isEnabled();
            case 7 -> this.slot8.isEnabled();
            case 8 -> this.slot9.isEnabled();
            default -> false;
        };
    }

    private boolean isProtectedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.TOTEM_OF_UNDYING && stack.hasEnchantments()) {
            return true;
        }
        if (stack.getItem() == Items.PLAYER_HEAD) {
            return true;
        }
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            return blockItem.getBlock() == Blocks.PLAYER_HEAD || blockItem.getBlock() == Blocks.PLAYER_WALL_HEAD;
        }
        return false;
    }

    private int getSlotIndex(Slot slot) {
        Object value;
        try {
            Method method = slot.getClass().getMethod("getIndex", new Class[0]);
            value = method.invoke((Object)slot, new Object[0]);
            if (value instanceof Integer) {
                Integer index = (Integer)value;
                return index;
            }
        }
        catch (Exception method) {
            
        }
        try {
            Field field = slot.getClass().getField("index");
            value = field.get(slot);
            if (value instanceof Integer) {
                Integer index = (Integer)value;
                return index;
            }
        }
        catch (Exception exception) {
            
        }
        return -1;
    }
}

