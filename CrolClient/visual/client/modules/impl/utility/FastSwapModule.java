
package crol.client.modules.impl.utility;

import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.PlayerEntity;
import net.minecraft.SlotActionType;
import net.minecraft.Slot;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Packet;
import net.minecraft.UpdateSelectedSlotC2SPacket;
import net.minecraft.KeyBinding;
import net.minecraft.MinecraftClient;
import net.minecraft.InputUtil;

public final class FastSwapModule {
    private static final String CATEGORY = "category.CrolClient.fastswap";
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final List<SwapBind> binds;
    private boolean registered;
    private long tickCounter;
    private long nextSwapTick;
    private int swapDelayTicks;

    public FastSwapModule() {
        this(3);
    }

    public FastSwapModule(int swapDelayTicks) {
        this.swapDelayTicks = Math.max(1, swapDelayTicks);
        this.binds = List.of(this.createBind("pearl", Items.ENDER_PEARL), this.createBind("trapka", Items.NETHERITE_SCRAP), this.createBind("plast", Items.DRIED_KELP), this.createBind("dezka", Items.ENDER_EYE), this.createBind("yavka", Items.SUGAR), this.createBind("fire_charge", Items.FIRE_CHARGE), this.createBind("god_aura", Items.PHANTOM_MEMBRANE), this.createBind("wind_charge", Items.WIND_CHARGE), this.createBind("snowball", Items.SNOWBALL));
    }

    public void register() {
        if (this.registered) {
            return;
        }
        this.registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
    }

    public void setSwapDelayTicks(int swapDelayTicks) {
        this.swapDelayTicks = Math.max(1, swapDelayTicks);
    }

    private SwapBind createBind(String id, Item item) {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding((KeyBinding)new KeyBinding("key.CrolClient.fastswap." + id, InputUtil.Type.KEYSYM, -1, CATEGORY));
        return new SwapBind(item, keyBinding);
    }

    private void onEndClientTick(MinecraftClient client) {
        ++this.tickCounter;
        if (client.player == null || client.world == null || client.interactionManager == null) {
            this.clearQueuedPresses();
            return;
        }
        if (client.currentScreen != null) {
            this.clearQueuedPresses();
            return;
        }
        for (SwapBind bind : this.binds) {
            if (!bind.keyBinding().wasPressed()) continue;
            while (bind.keyBinding().wasPressed()) {
            }
            if (this.tickCounter < this.nextSwapTick) {
                return;
            }
            if (this.trySwap(bind.item())) {
                this.nextSwapTick = this.tickCounter + (long)this.swapDelayTicks;
            }
            return;
        }
    }

    private void clearQueuedPresses() {
        for (SwapBind bind : this.binds) {
            while (bind.keyBinding().wasPressed()) {
            }
        }
    }

    private boolean trySwap(Item targetItem) {
        if (this.client.player == null || this.client.interactionManager == null) {
            return false;
        }
        if (this.isAlreadySelected(targetItem)) {
            return false;
        }
        int hotbarSlot = this.findItemInHotbar(targetItem);
        if (hotbarSlot != -1) {
            return this.selectHotbarSlot(hotbarSlot);
        }
        int inventorySlot = this.findItemInInventory(targetItem);
        if (inventorySlot == -1) {
            return false;
        }
        int targetHotbarSlot = this.findEmptyHotbarSlot();
        if (targetHotbarSlot == -1) {
            targetHotbarSlot = this.client.player.getInventory().selectedSlot;
        }
        return this.moveInventoryItemToHotbar(inventorySlot, targetHotbarSlot) && this.selectHotbarSlot(targetHotbarSlot);
    }

    private boolean isAlreadySelected(Item targetItem) {
        int selectedSlot = this.client.player.getInventory().selectedSlot;
        ItemStack stack = this.client.player.getInventory().getStack(selectedSlot);
        return !stack.isEmpty() && stack.getItem() == targetItem;
    }

    private int findItemInHotbar(Item targetItem) {
        return this.findItem(targetItem, 0, 9);
    }

    private int findItemInInventory(Item targetItem) {
        return this.findItem(targetItem, 9, 36);
    }

    private int findItem(Item targetItem, int start, int end) {
        for (int slot = start; slot < end; ++slot) {
            ItemStack stack = this.client.player.getInventory().getStack(slot);
            if (stack.isEmpty() || stack.getItem() != targetItem) continue;
            return slot;
        }
        return -1;
    }

    private int findEmptyHotbarSlot() {
        for (int slot = 0; slot < 9; ++slot) {
            if (!this.client.player.getInventory().getStack(slot).isEmpty()) continue;
            return slot;
        }
        return -1;
    }

    private boolean selectHotbarSlot(int slot) {
        if (slot < 0 || slot > 8) {
            return false;
        }
        if (this.client.player.getInventory().selectedSlot == slot) {
            return true;
        }
        this.client.player.getInventory().selectedSlot = slot;
        if (this.client.getNetworkHandler() != null) {
            this.client.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot));
        }
        return true;
    }

    private boolean moveInventoryItemToHotbar(int inventorySlot, int hotbarSlot) {
        if (inventorySlot < 9 || inventorySlot > 35 || hotbarSlot < 0 || hotbarSlot > 8) {
            return false;
        }
        if (this.client.player.currentScreenHandler != this.client.player.playerScreenHandler) {
            return false;
        }
        Slot sourceSlot = this.findPlayerInventorySlot(inventorySlot);
        if (sourceSlot == null) {
            return false;
        }
        this.client.interactionManager.clickSlot(this.client.player.playerScreenHandler.syncId, sourceSlot.id, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.client.player);
        return true;
    }

    private Slot findPlayerInventorySlot(int inventoryIndex) {
        for (Slot slot : this.client.player.playerScreenHandler.slots) {
            if (slot.inventory != this.client.player.getInventory() || slot.getIndex() != inventoryIndex) continue;
            return slot;
        }
        return null;
    }

    private record SwapBind(Item item, KeyBinding keyBinding) {
    }
}

