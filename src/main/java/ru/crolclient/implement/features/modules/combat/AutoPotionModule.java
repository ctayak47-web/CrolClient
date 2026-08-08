package ru.crolclient.implement.features.modules.combat;

import ru.crolclient.common.util.math.StopWatch;
import ru.crolclient.common.util.task.TaskPriority;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.Angle;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.RotationConfig;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.RotationController;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.util.Hand;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.SelectSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.implement.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoPotionModule extends Module {
    final SelectSetting throwModeSetting = new SelectSetting("Throw Mode", "Method of throwing potions")
            .value("Packet", "Legit");

    final ValueSetting throwDelaySetting = new ValueSetting("Throw Delay", "Delay between potion throws (ms)")
            .setValue(200.0F)
            .range(50.0F, 1000.0F)
            .increment(50.0F);

    final StopWatch throwTimer = new StopWatch();
    int previousSlot = -1;
    boolean throwing;
    Angle originalRotation;
    boolean isRotating;
    int rotationTicks;
    boolean hasSetRotation = false;

    public AutoPotionModule() {
        super("AutoPotion", "Auto Potion", ModuleCategory.COMBAT);
        setup(throwModeSetting, throwDelaySetting);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!isWorldLoaded() || !throwTimer.isReached((long) throwDelaySetting.getValue())) {
            return;
        }

        if (!hasSetRotation && !throwing && findSplashPotion() != -1) {
            originalRotation = RotationController.INSTANCE.getRotation();
            Angle downRotation = new Angle(originalRotation.getYaw(), 90.0f);

            RotationController.INSTANCE.rotateTo(
                    downRotation,
                    new RotationConfig(false, true, true),
                    TaskPriority.CRUCIAL_FOR_PLAYER_LIFE,
                    this
            );

            hasSetRotation = true;
            isRotating = true;
            rotationTicks = 0;
            return;
        }

        if (isRotating) {
            rotationTicks++;
            if (rotationTicks >= 2) {
                throwing = true;
                isRotating = false;
                rotationTicks = 0;
            }
            return;
        }

        if (throwing) {
            handleThrowing();
        }
    }

    private void handleThrowing() {
        int nextPotionSlot = findSplashPotion();
        if (nextPotionSlot != -1) {
            Angle downRotation = new Angle(originalRotation.getYaw(), 90.0f);
            RotationController.INSTANCE.rotateTo(
                    downRotation,
                    new RotationConfig(false, true, true),
                    TaskPriority.CRUCIAL_FOR_PLAYER_LIFE,
                    this
            );

            throwPotion(nextPotionSlot);
            throwTimer.reset();
        } else {
            if (previousSlot != -1) {
                restorePreviousSlot();
            }

            throwing = false;
            if (originalRotation != null) {
                RotationController.INSTANCE.rotateTo(
                        originalRotation,
                        new RotationConfig(false, true, true),
                        TaskPriority.HIGH_IMPORTANCE_1,
                        this
                );
                originalRotation = null;
                hasSetRotation = false;
            }
        }
    }

    private int findSplashPotion() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() != Items.SPLASH_POTION) continue;

            boolean needsPotion = false;
            PotionContentsComponent potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents != null && potionContents.hasEffects()) {
                needsPotion = java.util.stream.StreamSupport.stream(potionContents.getEffects().spliterator(), false)
                        .filter(effect -> effect.getEffectType().value().isBeneficial())
                        .anyMatch(effect -> {
                            StatusEffectInstance active = mc.player.getStatusEffect(effect.getEffectType());
                            return active == null || active.getDuration() < 20;
                        });
            }

            if (needsPotion) return i;
        }
        return -1;
    }

    private void throwPotion(int slot) {
        boolean isPacket = throwModeSetting.isSelected("Packet");
        ClientPlayerEntity player = mc.player;

        if (previousSlot == -1) {
            previousSlot = player.getInventory().selectedSlot;
        }

        if (isPacket) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, 0.0f, 0.0f));
        } else {
            player.getInventory().selectedSlot = slot;
            mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
        }
    }

    private void restorePreviousSlot() {
        if (throwModeSetting.isSelected("Packet")) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        } else {
            mc.player.getInventory().selectedSlot = previousSlot;
        }
        previousSlot = -1;
    }

    @Override
    public void deactivate() {
        if (originalRotation != null) {
            RotationController.INSTANCE.rotateTo(
                    originalRotation,
                    new RotationConfig(false, true, true),
                    TaskPriority.HIGH_IMPORTANCE_1,
                    this
            );
        }

        isRotating = false;
        hasSetRotation = false;
        rotationTicks = 0;
        throwing = false;

        super.deactivate();
    }

    private boolean isWorldLoaded() {
        return mc.world != null && mc.player != null && mc.interactionManager != null;
    }
}