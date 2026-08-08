package ru.crolclient.implement.features.modules.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BindSetting;
import ru.crolclient.common.util.math.Counter;
import ru.crolclient.common.util.player.PlayerInventoryUtil;
import ru.crolclient.common.util.task.TaskPriority;
import ru.crolclient.implement.events.keyboard.KeyEvent;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.Angle;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.RotationController;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("all")
public class ClickPearlModule extends Module {
    Counter counter = Counter.create();
    Runnable keyPressedAction;

    private final BindSetting bindSetting = new BindSetting("Throw", "Set the throw button");

    public Action action = Action.START;

    public ClickPearlModule() {
        super("ClickPearl", "Click Pearl", ModuleCategory.PLAYER);
        setup(bindSetting);
    }

    @EventHandler
    public void onKey(KeyEvent keyEvent) {
        if (keyEvent.isKeyDown(bindSetting.getKey()) && canPerformAction()) {
            action = Action.START;
            keyPressedAction = () -> performAction();
            counter.resetCounter();
        }
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (keyPressedAction != null) {
            keyPressedAction.run();
        }
    }

    private boolean canPerformAction() {
        return keyPressedAction == null && !mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL));
    }

    private void startAction() {
        action = Action.START;
        keyPressedAction = () -> performAction();
        counter.resetCounter();
    }

    private void performAction() {
        RotationController rotationController = RotationController.INSTANCE;

        Integer slot = PlayerInventoryUtil.INSTANCE.findHotbarSlot(Items.ENDER_PEARL);
        Hand hand = mc.player.getOffHandStack().getItem() instanceof EnderPearlItem ? Hand.OFF_HAND : Hand.MAIN_HAND;

        if (slot != null) {
            if (rotationController.getCurrentRotationPlan() != null) {
                interactWithRotation(slot, hand, rotationController);
            } else {
                interact(slot, hand);
            }
        } else {
            keyPressedAction = null;
        }
    }

    private void interactWithRotation(Integer slot, Hand hand, RotationController rotationController) {
        if (action == Action.START) {
            rotationController.rotateTo(new Angle(mc.player.getYaw(), mc.player.getPitch()),
                    RotationConfig.DEFAULT,
                    TaskPriority.HIGH_IMPORTANCE_2, this);
            switchSlotIfNeeded(slot, slot, hand);
            action = Action.WAIT;
        } else if (action == Action.WAIT && counter.isReached(70L)) {
            action = Action.USE_ITEM;
        } else if (action == Action.USE_ITEM) {
            interactWithItem(hand);
            switchSlotIfNeeded(mc.player.getInventory().selectedSlot, slot, hand);
            keyPressedAction = null;
        }
    }

    private void interact(Integer slot, Hand hand) {
        if (action == Action.START) {
            switchSlotIfNeeded(slot, slot, hand);
            action = Action.WAIT;
        } else if (action == Action.WAIT && counter.isReached(50L)) {
            action = Action.USE_ITEM;
        } else if (action == Action.USE_ITEM) {
            interactWithItem(hand);
            switchSlotIfNeeded(mc.player.getInventory().selectedSlot, slot, hand);
            keyPressedAction = null;
        }
    }

    private void switchSlotIfNeeded(Integer swapSlot, Integer slot, Hand hand) {
        if (slot != mc.player.getInventory().selectedSlot && hand != Hand.OFF_HAND) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(swapSlot));
        }
    }

    private void interactWithItem(Hand hand) {
        mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(hand, sequence, 0.0f, 0.0f));
        mc.player.swingHand(hand);
    }

    public enum Action {
        START, WAIT, USE_ITEM
    }
}