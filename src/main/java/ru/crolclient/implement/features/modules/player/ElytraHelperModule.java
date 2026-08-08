package ru.crolclient.implement.features.modules.player;

import ru.crolclient.api.event.events.Event;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BindSetting;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.common.util.player.InventoryHandler;
import ru.crolclient.common.util.player.MovingUtil;
import ru.crolclient.common.util.player.PlayerInventoryUtil;
import ru.crolclient.common.util.math.Counter;
import ru.crolclient.implement.events.keyboard.KeyEvent;
import ru.crolclient.implement.events.player.MovementInputEvent;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.implement.events.player.UpdatePlayerEvent;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Formatting;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ElytraHelperModule extends Module {
    final ValueSetting tickSetting = new ValueSetting("Tick", "Delay in ticks")
            .setValue(1).range(0, 2).increment(1);

    final BindSetting swapBindSetting = new BindSetting("Swap key", "Swaps elytra when pressed");
    final BindSetting boostBindSetting = new BindSetting("Boost key", "Uses firework when pressed");

    int swapTicks;
    boolean swapping;

    Counter counter = Counter.create();
    Runnable keyPressedAction;
    Action action = Action.START;

    public ElytraHelperModule() {
        super("ElytraHelper", ModuleCategory.PLAYER);
        setup(tickSetting, swapBindSetting, boostBindSetting);
    }

    @EventHandler
    public void onKey(KeyEvent keyEvent) {
        if (!isWorldLoaded()) return;

        if (keyEvent.isKeyDown(swapBindSetting.getKey())) {
            swapping = true;
        }

        if (keyEvent.isKeyDown(boostBindSetting.getKey()) && canPerformAction()) {
            action = Action.START;
            keyPressedAction = () -> performFireworkAction();
            counter.resetCounter();
        }
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (keyPressedAction != null) {
            keyPressedAction.run();
        }
    }

    @EventHandler
    public void onInput(MovementInputEvent event) {
        if (!isWorldLoaded()) return;

        if (swapping) {
            int elytraSlot = InventoryHandler.findItemSlot(Items.ELYTRA);
            boolean hasElytraEquipped = mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA;

            if (elytraSlot == -1 && !hasElytraEquipped) {
                logDirect(Formatting.RED + "Элитра не найдена");
                swapping = false;
                return;
            }

            if (swapTicks >= tickSetting.getValue()) {
                swap();
                swapTicks = 0;
                swapping = false;
            }

            if (tickSetting.getValue() > 0) {
                event.setDirectionalInput(MovingUtil.DirectionalInput.NONE);
            }

            swapTicks++;
        }
    }

    private boolean canPerformAction() {
        return keyPressedAction == null;
    }

    private void performFireworkAction() {
        Hand hand = mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET ?
            Hand.OFF_HAND : Hand.MAIN_HAND;

        Integer slot = hand == Hand.MAIN_HAND ?
            PlayerInventoryUtil.INSTANCE.findHotbarSlot(Items.FIREWORK_ROCKET) : null;

        if (slot != null || hand == Hand.OFF_HAND) {
            useFirework(slot, hand);
        } else {
            logDirect(Formatting.RED + "Фейерверки не найдены");
            keyPressedAction = null;
        }
    }

    private void useFirework(Integer slot, Hand hand) {
        if (action == Action.START) {
            if (hand == Hand.MAIN_HAND) {
                switchSlotIfNeeded(slot, slot, hand);
            }
            action = Action.WAIT;
        } else if (action == Action.WAIT && counter.isReached(50L)) {
            action = Action.USE_ITEM;
        } else if (action == Action.USE_ITEM) {
            interactWithItem(hand);
            if (hand == Hand.MAIN_HAND) {
                switchSlotIfNeeded(mc.player.getInventory().selectedSlot, slot, hand);
            }
            keyPressedAction = null;
        }
    }

    private void switchSlotIfNeeded(Integer swapSlot, Integer slot, Hand hand) {
        if (slot != mc.player.getInventory().selectedSlot && hand != Hand.OFF_HAND) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(swapSlot));
        }
    }

    private void interactWithItem(Hand hand) {
        mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                new PlayerInteractItemC2SPacket(hand, sequence, 0.0f, 0.0f));
        mc.player.swingHand(hand);
    }

    private void swap() {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        ClientPlayerInteractionManager interactionManager = mc.interactionManager;
        if (interactionManager == null) return;

        int syncId = player.currentScreenHandler.syncId;
        int chestSlot = 6;

        boolean hasElytraEquipped = player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA;

        if (hasElytraEquipped) {
            int chestplateSlot = InventoryHandler.findItemSlot(Items.NETHERITE_CHESTPLATE);
            if (chestplateSlot == -1) {
                chestplateSlot = InventoryHandler.findItemSlot(Items.DIAMOND_CHESTPLATE);
            }

            if (chestplateSlot != -1) {
                InventoryHandler.moveItem(chestplateSlot, chestSlot, true);
            } else {
                int emptySlot = InventoryHandler.findEmptySlot();
                if (emptySlot != -1) {
                    InventoryHandler.moveItem(chestSlot, emptySlot, false);
                } else {
                    logDirect(Formatting.RED + "Нет свободного места в инвентаре");
                }
            }
        } else {
            int elytraSlot = InventoryHandler.findItemSlot(Items.ELYTRA);
            if (elytraSlot == -1) {
                logDirect(Formatting.RED + "Элитра не найдена");
                return;
            }

            boolean hasChestplate = player.getInventory().getArmorStack(2).getItem() == Items.NETHERITE_CHESTPLATE
                    || player.getInventory().getArmorStack(2).getItem() == Items.DIAMOND_CHESTPLATE;

            if (hasChestplate) {
                int emptySlot = InventoryHandler.findEmptySlot();
                if (emptySlot != -1) {
                    InventoryHandler.moveItem(chestSlot, emptySlot, false);
                    InventoryHandler.moveItem(elytraSlot, chestSlot, false);
                } else {
                    logDirect(Formatting.RED + "Нет свободного места для нагрудника");
                }
            } else {
                InventoryHandler.moveItem(elytraSlot, chestSlot, false);
            }
        }
    }

    private boolean isWorldLoaded() {
        return mc.world != null && mc.player != null && mc.interactionManager != null;
    }

    private enum Action {
        START, WAIT, USE_ITEM
    }
}