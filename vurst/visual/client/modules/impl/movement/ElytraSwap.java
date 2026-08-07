
package vurst.visual.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.EquippableComponent;
import net.minecraft.EquipmentSlot;
import net.minecraft.PlayerEntity;
import net.minecraft.SlotActionType;
import net.minecraft.Slot;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.Screen;
import net.minecraft.InventoryScreen;
import net.minecraft.DataComponentTypes;
import org.lwjgl.glfw.GLFW;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.KeySetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.game.other.MessageUtil;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;
import vurst.visual.utility.game.player.PlayerInventoryComponent;
import vurst.visual.utility.game.player.PlayerInventoryUtil;
import vurst.visual.utility.math.StopWatch;

@ModuleAnnotation(name="ElytraSwap", category=Category.MOVEMENT, description="Меняет элитры в нагрудном слоте.")
public final class ElytraSwap
extends Module {
    public static final ElytraSwap INSTANCE = new ElytraSwap();
    private static final int CHEST_SLOT_ID = 6;
    private static final int OPEN_WAIT_TICKS = 5;
    private final KeySetting swapKey = new KeySetting("Клавиша свапа", -1);
    private final NumberSetting swapDelay = new NumberSetting("Задержка свапа", 2.0f, 0.0f, 10.0f, 1.0f);
    private final BooleanSetting autoFlyOnElytra = new BooleanSetting("Авто /fly (Князь)", false);
    private final StopWatch swapTimer = new StopWatch();
    private boolean wasSwapPressed;
    private boolean swapQueued;
    private boolean taskScheduled;
    private boolean inventoryOpened;
    private int swapTick;
    private int openWaitTicks;
    private ItemStack oldChestItem = ItemStack.EMPTY;

    private ElytraSwap() {
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (ElytraSwap.mc.player == null || ElytraSwap.mc.world == null) {
            return;
        }
        this.handleSwapInput();
    }

    private void handleSwapInput() {
        if (this.swapQueued) {
            this.processQueuedSwap();
            return;
        }
        boolean pressed = PlayerIntersectionUtil.isKey(this.swapKey);
        if (pressed && !this.wasSwapPressed && this.canSwap()) {
            this.swapQueued = true;
            this.taskScheduled = false;
            this.inventoryOpened = false;
            this.swapTick = 0;
            this.openWaitTicks = 0;
            this.processQueuedSwap();
        }
        this.wasSwapPressed = pressed;
    }

    private boolean canSwap() {
        long delayMs = Math.round(this.swapDelay.getCurrent() * 225.0f);
        return delayMs <= 0L || this.swapTimer.getElapsedTime() >= delayMs;
    }

    private void processQueuedSwap() {
        if (ElytraSwap.mc.currentScreen == null) {
            mc.setScreen((Screen)new InventoryScreen((PlayerEntity)ElytraSwap.mc.player));
            this.inventoryOpened = true;
            this.openWaitTicks = 0;
            return;
        }
        if (!this.inventoryOpened || !(ElytraSwap.mc.currentScreen instanceof InventoryScreen)) {
            if (this.inventoryOpened && this.openWaitTicks < 5) {
                ++this.openWaitTicks;
                return;
            }
            this.resetQueuedSwap();
            return;
        }
        this.openWaitTicks = 0;
        if (ElytraSwap.mc.player.currentScreenHandler == null || ElytraSwap.mc.player.currentScreenHandler != ElytraSwap.mc.player.playerScreenHandler) {
            return;
        }
        if (ElytraSwap.mc.player.currentScreenHandler.slots.size() <= 6) {
            return;
        }
        if (this.taskScheduled) {
            return;
        }
        this.taskScheduled = true;
        PlayerInventoryComponent.addTask(() -> {
            if (!this.swapQueued || ElytraSwap.mc.player == null) {
                this.taskScheduled = false;
                return;
            }
            this.applyMovementKeys();
            if (this.swapTick >= 1) {
                this.swapElytra();
                this.swapTimer.reset();
                this.closeInventory();
                this.resetQueuedSwap();
            } else {
                ++this.swapTick;
                this.releaseMovementKeys();
            }
            this.taskScheduled = false;
        });
    }

    private void closeInventory() {
        if (ElytraSwap.mc.currentScreen instanceof InventoryScreen) {
            PlayerInventoryUtil.closeScreen(true);
            mc.setScreen(null);
        }
    }

    private void resetQueuedSwap() {
        this.swapQueued = false;
        this.inventoryOpened = false;
        this.swapTick = 0;
        this.openWaitTicks = 0;
        this.taskScheduled = false;
    }

    private void swapElytra() {
        ItemStack chestItem = ElytraSwap.mc.player.getEquippedStack(EquipmentSlot.CHEST);
        Slot elytraSlot = PlayerInventoryUtil.getSlot(Items.ELYTRA);
        if (elytraSlot == null) {
            MessageUtil.displayError("Elytra not found.");
            return;
        }
        if (this.shouldEquipElytra(chestItem)) {
            this.oldChestItem = chestItem.copy();
            this.moveItemToChest(elytraSlot);
            this.trySendFlyCommand();
        } else {
            Slot oldSlot = null;
            if (!this.oldChestItem.isEmpty()) {
                oldSlot = PlayerInventoryUtil.getSlot(this.oldChestItem.getItem());
            }
            if (oldSlot == null) {
                oldSlot = this.findFallbackChestArmor();
            }
            if (oldSlot != null) {
                this.moveItemToChest(oldSlot);
            }
        }
    }

    private Slot findFallbackChestArmor() {
        return PlayerInventoryUtil.getSlot(slot -> {
            if (slot.id == 6) {
                return false;
            }
            ItemStack stack = slot.getStack();
            if (stack == null || stack.isEmpty() || stack.getItem() == Items.ELYTRA) {
                return false;
            }
            EquippableComponent equippable = (EquippableComponent)stack.get(DataComponentTypes.EQUIPPABLE);
            return equippable != null && equippable.comp_3174() == EquipmentSlot.CHEST;
        });
    }

    private void moveItemToChest(Slot fromSlot) {
        if (fromSlot == null) {
            return;
        }
        int from = fromSlot.id;
        int count = ElytraSwap.mc.player.currentScreenHandler.slots.size() - 9;
        if (from >= count && count == 36) {
            PlayerInventoryUtil.clickSlot(6, from - count, SlotActionType.SWAP, false);
            return;
        }
        PlayerInventoryUtil.moveItem(from, 6, false);
    }

    private boolean shouldEquipElytra(ItemStack chestItem) {
        return chestItem.getItem() != Items.ELYTRA;
    }

    private void trySendFlyCommand() {
        if (!this.autoFlyOnElytra.isEnabled() || ElytraSwap.mc.player == null || ElytraSwap.mc.player.networkHandler == null) {
            return;
        }
        ElytraSwap.mc.player.networkHandler.sendChatCommand("fly");
    }

    private void applyMovementKeys() {
        if (this.isWPressed()) {
            ElytraSwap.mc.options.forwardKey.setPressed(true);
        }
        if (this.isAPressed()) {
            ElytraSwap.mc.options.leftKey.setPressed(true);
        }
        if (this.isDPressed()) {
            ElytraSwap.mc.options.rightKey.setPressed(true);
        }
        if (this.isSPressed()) {
            ElytraSwap.mc.options.backKey.setPressed(true);
        }
        if (this.isJumpPressed()) {
            ElytraSwap.mc.options.jumpKey.setPressed(true);
        }
    }

    private void releaseMovementKeys() {
        ElytraSwap.mc.options.jumpKey.setPressed(false);
        ElytraSwap.mc.options.forwardKey.setPressed(false);
        ElytraSwap.mc.options.leftKey.setPressed(false);
        ElytraSwap.mc.options.rightKey.setPressed(false);
        ElytraSwap.mc.options.backKey.setPressed(false);
    }

    private boolean isWPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)87) == 1;
    }

    private boolean isAPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)65) == 1;
    }

    private boolean isDPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)68) == 1;
    }

    private boolean isSPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)83) == 1;
    }

    private boolean isJumpPressed() {
        long window = mc.getWindow().getHandle();
        return GLFW.glfwGetKey((long)window, (int)32) == 1;
    }
}

