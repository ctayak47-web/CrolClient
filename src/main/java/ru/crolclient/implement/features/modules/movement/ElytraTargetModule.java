package ru.crolclient.implement.features.modules.movement;

import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.*;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.common.util.target.TargetSelector;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.implement.events.player.PostRotationMovementInputEvent;
import ru.crolclient.implement.features.modules.combat.AuraModule;
import ru.crolclient.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.crolclient.implement.features.modules.combat.killaura.attack.ClickScheduler;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.*;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.angle.*;
import ru.crolclient.common.util.task.TaskPriority;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ElytraTargetModule extends Module {
    final TargetSelector targetSelector = new TargetSelector();
    final AngleSmoothMode smoothMode = new ElytraAdaptiveSmoothMode();

    final ValueSetting maxDistanceSetting = new ValueSetting("Max Distance", "Maximum target search distance")
            .setValue(100.0f).range(10.0f, 100.0f).increment(1.0f);

    final BooleanSetting autoFirework = new BooleanSetting("Auto Firework", "Automatically uses firework rockets");

    final ValueSetting fireworkDelay = new ValueSetting("Firework Delay", "Delay between firework uses (in ticks)")
            .setValue(20.0f).range(1.0f, 100.0f).increment(1.0f);

    final MultiSelectSetting targetTypeSetting = new MultiSelectSetting("Target Type", "Filters the entire list of targets by type")
            .value(Stream.of(TargetSelector.TargetType.values())
                    .map(TargetSelector.TargetType::getDisplayName)
                    .toArray(String[]::new));

    final SelectSetting followMode = new SelectSetting("Follow Mode", "Select target following mode")
            .value("Rotation", "Center");

    final MultiSelectSetting flightSettings = new MultiSelectSetting("Flight Settings", "Additional flight configuration options")
            .value(
                "Sync with Aura",
                "Through Walls",
                "Smart Height",
                "Predict Movement",
                "Auto Distance",
                "Boost near Target",
                "Safe Mode"
            );

    int fireworkTicks = 0;
    LivingEntity target = null;

    AuraModule auraModule;

    public ElytraTargetModule() {
        super("ElytraTarget", ModuleCategory.MOVEMENT);
        setup(maxDistanceSetting, autoFirework, fireworkDelay, targetTypeSetting, followMode, flightSettings);
    }

    @EventHandler
    public void onPostRotationMovementInput(PostRotationMovementInputEvent event) {
        if (mc.player == null || !mc.player.isGliding()) {
            return;
        }

        target = updateTarget();
        if (target != null) {
            rotateToTarget(target);
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || !mc.player.isGliding() || target == null) {
            return;
        }

        if (flightSettings.isSelected("Boost near Target") && target.distanceTo(mc.player) < 20) {
            if (mc.player.getVelocity().length() < 2.0) {
                mc.player.addVelocity(
                    mc.player.getRotationVector().x * 0.1,
                    mc.player.getRotationVector().y * 0.1,
                    mc.player.getRotationVector().z * 0.1
                );
            }
        }

        if (flightSettings.isSelected("Safe Mode")) {
            Vec3d nextPos = mc.player.getPos().add(mc.player.getVelocity());
            Box nextBox = mc.player.getBoundingBox().offset(mc.player.getVelocity());
            
            if (!mc.world.isSpaceEmpty(nextBox)) {
                mc.player.addVelocity(0, 0.1, 0);
            }
        }

        if (autoFirework.isValue()) {
            fireworkTicks++;

            boolean canUseFirework = true;
            
            if (fireworkTicks >= fireworkDelay.getValue()) {
                if (flightSettings.isSelected("Sync with Aura")) {
                    if (auraModule == null) {
                        auraModule = (AuraModule) Extra.getInstance().getModuleProvider().module("Aura");
                    }
                    
                    if (auraModule != null && auraModule.isState()) {
                        AttackHandler attackHandler = Extra.getInstance().getAttackPerpetrator().getAttackHandler();
                        ClickScheduler clickScheduler = attackHandler.getClickScheduler();
                        
                        if (target.distanceTo(mc.player) <= auraModule.getMaxDistanceSetting().getValue() 
                            && !clickScheduler.hasTicksElapsedSinceLastClick(2)) {
                            canUseFirework = false;
                        }
                    }
                }

                if (canUseFirework) {
                    int fireworkSlot = findFireworkSlot();

                    if (fireworkSlot != -1) {
                        int previousSlot = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = fireworkSlot;
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.getInventory().selectedSlot = previousSlot;
                        fireworkTicks = 0;
                    }
                }
            }
        }
    }

    private LivingEntity updateTarget() {
        Set<TargetSelector.TargetType> targetTypes = new HashSet<>();
        for (String selected : targetTypeSetting.getSelected()) {
            for (TargetSelector.TargetType type : TargetSelector.TargetType.values()) {
                if (type.getDisplayName().equals(selected)) {
                    targetTypes.add(type);
                    break;
                }
            }
        }

        return targetSelector.updateTarget(targetTypes, mc.world.getEntities(), maxDistanceSetting.getValue());
    }

    private void rotateToTarget(LivingEntity target) {
        RotationController rotationController = RotationController.INSTANCE;
        
        Vec3d targetPos;
        if (followMode.isSelected("Rotation")) {
            targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
            
            if (flightSettings.isSelected("Predict Movement")) {
                Vec3d velocity = target.getVelocity();
                targetPos = targetPos.add(velocity.multiply(2.0));
            }

            Vec3d randomOffset = smoothMode.randomValue();
            randomOffset = randomOffset.multiply(4.0);
            targetPos = targetPos.add(randomOffset);

            if (flightSettings.isSelected("Smart Height")) {
                double heightDiff = mc.player.getY() - target.getY();
                if (heightDiff < 5) {
                    targetPos = targetPos.add(0, 5 - heightDiff, 0);
                }
            }
        } else {
            targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
        }
        
        if (flightSettings.isSelected("Auto Distance")) {
            Vec3d directionToTarget = targetPos.subtract(mc.player.getPos()).normalize();
            double currentDistance = mc.player.getPos().distanceTo(targetPos);
            double idealDistance = 10.0;
            
            if (currentDistance < idealDistance) {
                targetPos = targetPos.subtract(directionToTarget.multiply(idealDistance - currentDistance));
            }
        }
        
        Vec3d playerPos = mc.player.getEyePos();
        Vec3d difference = targetPos.subtract(playerPos);
        
        Angle angle = AngleUtil.fromVec3d(difference);
        Angle.VecRotation vecRotation = new Angle.VecRotation(angle, targetPos);
        
        RotationConfig config = new RotationConfig(
            smoothMode,
            false,
            true,
            true
        );

        rotationController.rotateTo(
            vecRotation,
            target,
            config,
            TaskPriority.HIGH_IMPORTANCE_1,
            this
        );
    }

    private int findFireworkSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof FireworkRocketItem) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void deactivate() {
        fireworkTicks = 0;
        target = null;
        super.deactivate();
    }
}